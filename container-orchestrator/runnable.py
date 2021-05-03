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
environ["OAUTH_USER"] = "??"
environ["OAUTH_PASSWORD"] = "??"
environ["OAUTH_CLIENT_ID"] = "cmemc"
# use the following for 'client_credentials' OAUTH_GRANT_TYPE
# environ["OAUTH_GRANT_TYPE"] = "client_credentials"
# environ["OAUTH_CLIENT_ID"] = "cmem-service-account"
# environ["OAUTH_CLIENT_SECRET"] = "c8c12828-000c-467b-9b6d-2d6b5e16df4a"

# file 
deployment_state_sparql_template = "query.image.state.template.sparql"
update_deployment_state_sparql_template = "update.image.state.template.sparql"

# image states
REVIEWED_STATE = "https://braine.eccenca.dev/vocabulary/itops#ImageReviewed"
ACTIVATING_STATE = "https://braine.eccenca.dev/vocabulary/itops#ImageUnderActivation"
ACTIVE_STATE = "https://braine.eccenca.dev/vocabulary/itops#ImageActive"

# Container Orchestrator DEFAULT configuration
interval = 1 # default interval 

# Load itops transceiver configuration file from config.yaml file
transceiverConfig = None
try:
    with open("config.yaml", 'r') as stream:
        transceiverConfig = yaml.safe_load(stream)
except Exception as e:
    print('Exception when opening config.yaml' + ": %s\n" % e)

if('interval' in transceiverConfig):
    interval = transceiverConfig['interval']

def register(name, manifest):
    try:
        print("registering %s" + name)
        # register the image
        print("registered %s" + name)
    except Exception as e:
        print("Exception when registering the image" + ": %s" % e)

def getImages(state = None):
    try:
        f = open(deployment_state_sparql_template, 'r')
        sparql_query = f.read()
        # print(sparql_query)
        resultsRaw = SparqlQuery(sparql_query).get_results()
        resultsSet = json.loads(resultsRaw)
        images = resultsSet['results']
        return images['bindings']
    except Exception as e:
        print("Exception when retrieving reviewed docker images: %s" % e)

def updateState(id, state):
    try:
        f = open(update_deployment_state_sparql_template, 'r')
        sparql_query = f.read()
        sparql_query = sparql_query.replace("{state}", state)
        sparql_query = sparql_query.replace("{id}", id)
        # print(sparql_query)
        SparqlQuery(sparql_query).get_results()
    except Exception as e:
        print("Exception when updating image state" + ": %s" % e)

starttime = time.time()
while True:
    try:
        images = getImages()
        if(images != None):
            for image in images:
                if(image['state']['value'] == REVIEWED_STATE):
                    print('Reviewed Image found: %s' % image['label']['value'])
                    print("Starting image activation...")
                    updateState(image['id']['value'], ACTIVATING_STATE)
                    register(image['label']['value'], image['manifest']['value'])
                    updateState(image['id']['value'], ACTIVE_STATE)
                    print("Image successfully activated...")
                else:
                    print('No image to activate has been found...')
    except Exception as e:
        print("Exception when processing images: %s" % e)
    time.sleep(interval - ((time.time() - starttime) % interval))
