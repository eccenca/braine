# Sytem imports
import yaml, time, threading, json
# CMEM imports
from os import environ
from cmem.cmempy.workspace.projects.project import get_projects
from cmem.cmempy.workflow.workflow import execute_workflow_io
from cmem.cmempy.workspace.activities.taskactivity import get_activity_status
from cmem.cmempy.queries import SparqlQuery
# Kubernetes imports
from kubernetes import client, config, watch, client
from kubernetes.client.rest import ApiException
from confluent_kafka import Consumer, KafkaError
from kubernetes.client import ApiClient

# setup the environment for the connection to Corporate Memory
environ["CMEM_BASE_URI"] = "https://braine.eccenca.dev/" # target cmem instance
# use the following for 'password' OAUTH_GRANT_TYPE
environ["OAUTH_GRANT_TYPE"] = "password"
environ["OAUTH_USER"] = "dell"
environ["OAUTH_PASSWORD"] = "677e8dca667724d9ad645be8b9a9df91"
environ["OAUTH_CLIENT_ID"] = "cmemc"
# use the following for 'client_credentials' OAUTH_GRANT_TYPE
# environ["OAUTH_GRANT_TYPE"] = "client_credentials"
# environ["OAUTH_CLIENT_ID"] = "cmem-service-account"
# environ["OAUTH_CLIENT_SECRET"] = "c8c12828-000c-467b-9b6d-2d6b5e16df4a"

# Relay DEFAULT configuration
interval = 1 # default interval
version = "v1beta1" # default API version
kind = "node" # default kind of resource metrics
channels = ['stdout'] # default output channel 
namespace = None # default namespace
kubernetes_host = None
kubernetes_api_key = None
kubernetes_mode = 'list' # list or watch
kafka_host = "localhost:9092"
kafka_topic = 'k8s.metrics.nodes'
origin = "kubernetes"

# Load itops transceiver configuration file from config.yaml file
transceiverConfig = None
try:
    with open("config.yaml", 'r') as stream:
        transceiverConfig = yaml.safe_load(stream)
except Exception as e:
    print('Exception when opening config.yaml' + ": %s\n" % e)

# Setting Kubernetes config
if('group' in transceiverConfig):
    group = transceiverConfig['group']
if('version' in transceiverConfig):
    version = transceiverConfig['version']
if('kind' in transceiverConfig):
    kind = transceiverConfig['kind']
if('out' in transceiverConfig):
    channels = transceiverConfig['out']
if('pod.namespace' in transceiverConfig):
    namespace = transceiverConfig['pod.namespace']
if('interval' in transceiverConfig):
    interval = transceiverConfig['interval']
if('mode' in transceiverConfig):
    mode = transceiverConfig['mode']
if('origin' in transceiverConfig):
    origin = transceiverConfig['origin']
if('kubernetes.host' in transceiverConfig):
    kubernetes_host = transceiverConfig['kubernetes.host']
if('kubernetes.host' in transceiverConfig):
    kubernetes_host = transceiverConfig['kubernetes.host']
if('kubernetes.key' in transceiverConfig):
    kubernetes_api_key = transceiverConfig['kubernetes.key']
if('kafka.host' in transceiverConfig):
    kafka_host = transceiverConfig['kafka.host']
if('kubernetes.mode' in transceiverConfig):
    kubernetes_mode = transceiverConfig['kubernetes.mode']

# Setting CMEM config
if('cmem.update.graph' in transceiverConfig):
    update_sparql_graph = transceiverConfig['cmem.update.graph']
if('cmem.di.project.name' in transceiverConfig):
    cmem_di_project_name = transceiverConfig['cmem.di.project.name']
if('cmem.di.task.name' in transceiverConfig):
    cmem_di_task_name = transceiverConfig['cmem.di.task.name']

kafka_settings = {
    'bootstrap.servers': kafka_host,
    'group.id': 'mygroup',
    'client.id': 'client-1',
    'enable.auto.commit': True,
    'session.timeout.ms': 6000,
    'default.topic.config': {'auto.offset.reset': 'smallest'}
}

def out(nodeData):  
    print("%s" % (nodeData))

def cmem(nodeData):
    try:
        inputFile = "nodes.info.k8s.io.json"
        inMimFormat = "application/json"
        outMimFormat = "application/n-triples"
        f = open(inputFile, "w")
        f.write(json.dumps(nodeData))
        f.close()
        execute_workflow_io(cmem_di_project_name, cmem_di_task_name, inputFile, inMimFormat, outMimFormat)
    except Exception as e:
        print("Exception when broadcasting to CMEM" + ": %s" % e)

def streamK8sNodeMetrics(nodeInfos, channels, channelOptions):
    for channel in channels: # broadcast in different channels
        channelOptions[channel](nodeInfos)          

# Kubernetes configs
# Configs can be set in Configuration class directly or using helper utility
customConfig = None
if(kubernetes_host is None):
    config.load_kube_config()
else:
    customConfig = client.Configuration(host=kubernetes_host)
    if(kubernetes_api_key is not None):
        customConfig.api_key['authorization'] = kubernetes_api_key

# Output default options
channelOptions = {'stdout': out, 'cmem': cmem}

if(origin == "kubernetes"):
    coreAPIV1 = None
    if(customConfig is None):
        coreAPIV1 = client.CoreV1Api()
    else:
        with client.ApiClient(customConfig) as api_client:
            coreAPIV1 = client.CoreV1Api(api_client)
    w = watch.Watch()
    starttime = time.time()
    try:
        if kubernetes_mode == "list":
            node_list = coreAPIV1.list_node(async_req=False, _preload_content=False) # list nodes 
            serializedNodeList = ApiClient().sanitize_for_serialization(node_list.data)
            nodeInfos = json.loads(serializedNodeList)
            streamK8sNodeMetrics(nodeInfos, channels, channelOptions)
        else:
            for node_list in w.stream(coreAPIV1.list_node):
                serializedNodeList = ApiClient().sanitize_for_serialization(node_list)
                nodeInfos = json.dumps(serializedNodeList)
                streamK8sNodeMetrics(nodeInfos, channels, channelOptions)
    except ApiException as e:
        print("Exception when retrieving nodes information " + transceiverConfig['group'] + ": %s" % e)
else:
    c = Consumer(kafka_settings)
    c.subscribe([kafka_topic])
    try:
        while True:
            msg = c.poll(interval)
            try:
                if msg is None:
                    continue
                elif not msg.error():
                    value = json.loads(msg.value())
                    streamK8sNodeMetrics(value, channels, channelOptions)
                elif msg.error().code() == KafkaError._PARTITION_EOF:
                    print('End of partition reached {0}/{1}'
                        .format(msg.topic(), msg.partition()))
                else:
                    print('Error occured: {0}'.format(msg.error().str()))
            except Exception as e:
                print("Exception when decoding metrics: %s" % e)
    except KeyboardInterrupt:
        pass
    finally:
        c.close()