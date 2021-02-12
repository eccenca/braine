# Sytem imports
import yaml, time, threading
# CMEM imports
from os import environ
from cmem.cmempy.workspace.projects.project import get_projects
from cmem.cmempy.workflow import get_workflows
from cmem.cmempy.workspace.activities.taskactivity import get_activity_status
from cmem.cmempy.queries import SparqlQuery
# Kubernetes imports
from kubernetes import client, config, watch, client
from kubernetes.client.rest import ApiException

# setup the environment for the connection to Corporate Memory
environ["CMEM_BASE_URI"] = "https://braine.eccenca.dev/" # target cmem instance
# use the following for 'password' OAUTH_GRANT_TYPE
environ["OAUTH_GRANT_TYPE"] = "password"
environ["OAUTH_USER"] = "?"
environ["OAUTH_PASSWORD"] = "?"
environ["OAUTH_CLIENT_ID"] = "cmemc"
# use the following for 'client_credentials' OAUTH_GRANT_TYPE
# environ["OAUTH_GRANT_TYPE"] = "client_credentials"
# environ["OAUTH_CLIENT_ID"] = "cmem-service-account"
# environ["OAUTH_CLIENT_SECRET"] = "c8c12828-000c-467b-9b6d-2d6b5e16df4a"

# Relay DEFAULT configuration
interval = 1 # default interval
group = "metrics.k8s.io" # default metrics server group
version = "v1beta1" # default API version
kind = "node" # default kind of resource metrics
channels = ['stdout'] # default output channel 
nodeTemplateFile = "node.template.nt" # default node tempalte
podTemplateFile = "pod.tempalte.nt" # default pod template
namespace = None # default namespace
kubernetes_host = None
kubernetes_api_key = None

mode = "sync" # default output streaming mode

# CMEM configuration
node_triple_template = "node.template.nt"
pod_triple_templatee = "pod.template.nt"
update_sparql_template = "query.update.template.sparql"
update_sparql_graph = "https://vocab.eccenca.com/itops/kubernetes"

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
if('kubernetes.host' in transceiverConfig):
    kubernetes_host = transceiverConfig['kubernetes.host']
if('kubernetes.key' in transceiverConfig):
    kubernetes_api_key = transceiverConfig['kubernetes.key']

# Setting CMEM config
if('cmem.node.template' in transceiverConfig):
    node_triple_template = transceiverConfig['cmem.node.template']
if('cmem.pod.template' in transceiverConfig):
    pod_triple_template = transceiverConfig['cmem.pod.template']
if('cmem.update.template' in transceiverConfig):
    update_sparql_template = transceiverConfig['cmem.update.template']
if('cmem.update.graph' in transceiverConfig):
    update_sparql_graph = transceiverConfig['cmem.update.graph']


def out(kind, name, namespace, timestamp, window, cpu, memory):  
    print("%s\t%s\t%s\t%s\t%s\t%s\t%s" % (kind, name, namespace, timestamp, window, cpu, memory))

def cmem(kind, name, namespace, timestamp, window, cpu, memory):
    try:
        f = open(update_sparql_template, 'r')
        sparql_query = f.read().replace("{graph}", update_sparql_graph)
        triples = None
        if(kind == "node"):
            triples = node2Triple(name, timestamp, window, cpu, memory)
        else:
            triples = pod2Triple(name, namespace, timestamp, window, cpu, memory)
        sparql_query = sparql_query.replace("{telemetry}", triples)
        # print(sparql_query)
        SparqlQuery(sparql_query).get_results()
    except Exception as e:
        print("Exception when broadcasting to CMEM" + ": %s\n" % e)

def node2Triple(name, timestamp, window, cpu, memory):
    arguments = locals()
    try:
        f = open(node_triple_template, 'r')
        triples = f.read()
        triples = fillTemplate(arguments, triples)
        return triples
    except Exception as e:
        print("Exception when triplifying node" + name + ": %s\n" % e)

def pod2Triple(name, namespace, timestamp, window, cpu, memory):
    arguments = locals()
    try:
        f = open(pod_triple_template, 'r')
        triples = f.read()
        triples = fillTemplate(arguments, triples)
        return triples
    except Exception as e:
        print("Exception when triplifying node" + name + ": %s\n" % e)

def fillTemplate(arguments, template):
    for arg in arguments:
            value = arguments[arg]
            if(value is not None):
                template = template.replace("{" + arg + "}", value)
    return template

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
customAPIV1 = None
if(customConfig is None):
    customAPIV1 = client.CustomObjectsApi()
else:
    with client.ApiClient(customConfig) as api_client:
        customAPIV1 = client.CustomObjectsApi(api_client)
starttime = time.time()
try:
    while True:
        ret = None
        if(namespace is not None):
            ret = customAPIV1.list_namespaced_custom_object(group, version, namespace, kind + 's') # Just pod metrics for the default namespace
        else:
            ret = customAPIV1.list_cluster_custom_object(group, version, kind + 's') # All Metrics
        for i in ret['items']:
            iname = None
            inamespace = None
            itimestamp = None
            iwindow = None
            icpu = None
            imemory = None
            if(kind == "pod"):
                iname = i['containers'][0]['name']
                inamespace = i['metadata']['namespace']
                itimestamp = i['timestamp']
                iwindow = i['window'] # Window in seconds
                icpu = i['containers'][0]['usage']['cpu'] # CPU usage in millicores
                imemory = i['containers'][0]['usage']['memory'] # Memory in kibibytes (1024 bytes)
            else:
                iname = i['metadata']['name']
                itimestamp = i['timestamp']
                iwindow = i['window'] # Window in seconds
                icpu = i['usage']['cpu'] # CPU usage in millicores
                imemory = i['usage']['memory'] # Memory in kibibytes (1024 bytes)
            # removing datatype tags
            imemory = imemory.replace("Ki", "") 
            icpu = icpu.replace("n", "")
            iwindow = iwindow.replace("s", "")
            for channel in channels: # Broadcast in different channels
                if(mode == "async"):
                    thr = threading.Thread(target=channelOptions[channel], args=(kind, iname, inamespace, itimestamp, iwindow, icpu, imemory), kwargs={})
                    thr.start() # runs
                else:
                    channelOptions[channel](kind, iname, inamespace, itimestamp, iwindow, icpu, imemory)
        time.sleep(interval - ((time.time() - starttime) % interval))
except ApiException as e:
        print("Exception when retrieving metrics from 'group' " + transceiverConfig['group'] + ": %s\n" % e)