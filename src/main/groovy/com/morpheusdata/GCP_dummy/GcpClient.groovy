package com.morpheusdata.GCP_dummy

import com.google.api.client.json.JsonFactory
import com.morpheusdata.model.Cloud
import groovy.util.logging.Slf4j
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.http.HttpTransport
import com.google.api.services.cloudresourcemanager.CloudResourceManager
import com.google.api.services.compute.Compute
import com.google.auth.http.HttpCredentialsAdapter
import com.google.api.client.http.HttpRequestInitializer


@Slf4j
class GcpClient {
    GcpDummyPlugin gcpDummyPlugin
    private Cloud cloud

    GcpClient(GcpDummyPlugin gcpDummyPlugin,Cloud cloud) {
        this.gcpDummyPlugin = gcpDummyPlugin
        this.cloud = cloud
    }

    private getCloudResourceManagerClient(opts) {
        def credentials = getGoogleCredentials(opts,"https://www.googleapis.com/auth/cloud-platform")

        NetHttpTransport.Builder builder = new NetHttpTransport.Builder()
        if (cloud.apiProxy){
            def proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(cloud.apiProxy.proxyHost, cloud.apiProxy.proxyPort))
            builder.setProxy(proxy)
        }
        HttpTransport HTTP_TRANSPORTER = builder.build()
        JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance()
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials)

        return new CloudResourceManager.Builder(HTTP_TRANSPORTER, JSON_FACTORY, requestInitializer)
                .setApplicationName("Morpheus/1.0")
                .build()
    }

    private getComputeClient(opts) {
        def credentials = getGoogleCredentials(opts,"https://www.googleapis.com/auth/compute")
//
        NetHttpTransport.Builder builder = new NetHttpTransport.Builder()
        if (cloud.apiProxy){
            def proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(cloud.apiProxy.proxyHost, cloud.apiProxy.proxyPort))
            builder.setProxy(proxy)
        }
        HttpTransport HTTP_TRANSPORTER = builder.build()
        JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance()
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials)

        return new Compute.Builder(HTTP_TRANSPORTER, JSON_FACTORY, requestInitializer)
                .setApplicationName("Morpheus/1.0")
                .build()
    }

    private static getGoogleCredentials(opts,scope) {
        InputStream is = getJsonCredInputStream(opts)
        ServiceAccountCredentials.fromStream(is).createScoped(scope)
    }


    private static getJsonCredInputStream(opts) {

        def projectId = ""
        def clientEmail = opts.ClientEmail.toString()
        def privateKey = opts.PrivateKey.toString().replace('\r', '\\r').replace('\n', '\\n')

        String credentialsString = """
        {
          "type": "service_account",
          "project_id": "${projectId}",
          "private_key_id": "",
          "private_key": "${privateKey}",
          "client_email": "${clientEmail}",
          "client_id": "",
          "auth_uri": "https://accounts.google.com/o/oauth2/auth",
          "token_uri": "https://accounts.google.com/o/oauth2/token",
          "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs"
        }
        """

        InputStream is = new ByteArrayInputStream(credentialsString.getBytes())
    }

    def getProjectIds() {
        def projectIds = []
        def clientEmail = gcpDummyPlugin.ClientEmail
        def privateKey = gcpDummyPlugin.PrivateKey
        def opts = [ClientEmail: clientEmail, PrivateKey: privateKey]

        try{
            CloudResourceManager cloudResourceManagerClient = getCloudResourceManagerClient(opts)
            def response = cloudResourceManagerClient.projects().list().execute()
            projectIds = response.getProjects()?.findAll { it.getLifecycleState() == 'ACTIVE' }
        }
        catch (Exception e) {
            log.error("Error getting project id from credentials: ${e.message}", e)
        }
        return projectIds
    }

    def listVirtualMachines(String projectId){
        def instances = []
        def clientEmail = gcpDummyPlugin.ClientEmail
        def privateKey = gcpDummyPlugin.PrivateKey
        def opts = [ClientEmail: clientEmail, PrivateKey: privateKey]

        try{
            Compute computeClient = getComputeClient(opts)
            instances = computeClient.instances().aggregatedList(projectId).execute()
            log.info("Instances: ${instances}")
        }
        catch (Exception e) {
            log.error("Error getting instances from credentials: ${e.message}", e)
        }
        return instances
    }

    def deleteInstance(String projectId,String instanceId,String googleZoneId){
        def clientEmail = gcpDummyPlugin.ClientEmail
        def privateKey = gcpDummyPlugin.PrivateKey
        def opts = [ClientEmail: clientEmail, PrivateKey: privateKey]

        try{
            Compute computeClient = getComputeClient(opts)
            computeClient.instances().delete(projectId,googleZoneId,instanceId).execute()
        }
        catch (Exception e) {
            log.error("Error deleting instance from credentials: ${e.message}", e)
        }
    }

    def PowerOnInstance(String projectId,String instanceId,String googleZoneId){
        def clientEmail = gcpDummyPlugin.ClientEmail
        def privateKey = gcpDummyPlugin.PrivateKey
        def opts = [ClientEmail: clientEmail, PrivateKey: privateKey]

        try{
            Compute computeClient = getComputeClient(opts)
            computeClient.instances().start(projectId,googleZoneId,instanceId).execute()
        }
        catch (Exception e) {
            log.error("Error starting instance from credentials: ${e.message}", e)
        }
    }

    def PowerOffInstance(String projectId,String instanceId,String googleZoneId){
        def clientEmail = gcpDummyPlugin.ClientEmail
        def privateKey = gcpDummyPlugin.PrivateKey
        def opts = [ClientEmail: clientEmail, PrivateKey: privateKey]

        try{
            Compute computeClient = getComputeClient(opts)
            computeClient.instances().stop(projectId,googleZoneId,instanceId).execute()
        }
        catch (Exception e) {
            log.error("Error stopping instance from credentials: ${e.message}", e)
        }
    }

}
