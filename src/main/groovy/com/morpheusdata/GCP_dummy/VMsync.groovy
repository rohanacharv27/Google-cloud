package com.morpheusdata.GCP_dummy

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.util.SyncTask
import com.morpheusdata.model.Cloud
import com.morpheusdata.model.ComputeServer
import com.morpheusdata.model.projection.ComputeServerIdentityProjection
import com.morpheusdata.response.ServiceResponse
import com.morpheusdata.core.data.DataQuery
import com.morpheusdata.model.ComputeServerType
import groovy.util.logging.Slf4j

@Slf4j
class VmSync {

    private Cloud cloud
    GcpDummyPlugin gcpDummyPlugin
    private Boolean createNew
    private MorpheusContext morpheusContext

    VmSync(GcpDummyPlugin plugin, Cloud cloud) {
        this.gcpDummyPlugin = plugin
        this.cloud = cloud
        createNew = false
        this.morpheusContext = plugin.morpheusContext
    }

    def execute(){
        log.info "Executing VM Sync for ${cloud.name} ${cloud.id}"
        try {
            def inventoryLevel = cloud.getConfigProperty('importExisting')
            log.info("Inventory Level: ${inventoryLevel}")
            if(inventoryLevel == 'on' || inventoryLevel == 'true' || inventoryLevel == true) {
                createNew = true
            }

            GcpClient client = new GcpClient(gcpDummyPlugin,cloud)
		    def projectIds = client.getProjectIds()
		    def projectId = projectIds[0].projectId
            log.info("Project Id: ${projectId}")

            ServiceResponse response = getVmsFromApi(projectId)
            log.info("Response obtained : ${response}")

            if(response.success) {

                def domainRecords = morpheusContext.async.computeServer.listIdentityProjections(
                        new DataQuery().withFilter("account.id", cloud.account.id)
                                .withFilter("zone.id", cloud.id)
                )

                SyncTask<ComputeServerIdentityProjection, Map, ComputeServer> syncTask = new SyncTask<>(domainRecords, response.data as Collection<Map>)
                log.info("Adding new log to list")
                syncTask.addMatchFunction { ComputeServerIdentityProjection domainObject, Map cloudItem ->
                    domainObject.externalId == cloudItem.id.toString()
                }.withLoadObjectDetails { List<SyncTask.UpdateItemDto<ComputeServerIdentityProjection, Map>> updateItems ->
                    Map<Long, SyncTask.UpdateItemDto<ComputeServerIdentityProjection, Map>> updateItemMap = updateItems.collectEntries { [(it.existingItem.id): it] }
                    morpheusContext.async.computeServer.listById(updateItems?.collect { it.existingItem.id }).map { ComputeServer server ->
                        SyncTask.UpdateItemDto<ComputeServerIdentityProjection, Map> matchItem = updateItemMap[server.id]
                        return new SyncTask.UpdateItem<ComputeServer, Map>(existingItem: server, masterItem: matchItem.masterItem)
                    }
                }.onAdd { itemsToAdd ->
                    if(createNew) {
                        log.info("Items to add ${itemsToAdd}")
                        addMissingVirtualMachines(itemsToAdd as List,projectId)
                    }
                }.onUpdate { List<SyncTask.UpdateItem<ComputeServer, Map>> updateItems ->
//                    updateMatchedVirtualMachines(updateItems)
                }.onDelete { removeItems ->
                    removeMissingVirtualMachines(removeItems)
                }.observe().blockingSubscribe()
            } else {
                log.error("Error Caching VMs: {}", response.error)
            }
        } catch(Exception ex) {
            log.error("VirtualMachineSync error: {}", ex, ex)
        }
    }

     def buildVmConfig(Map cloudItem){
        def serverType = new ComputeServerType(code: 'googleUnmanaged',managed: false)
        log.info("ComputeServerType: ${serverType} , code :${serverType.code} , managed :${serverType.managed} ,power : ${serverType.controlPower} , delete : ${serverType.externalDelete}")

        def vmConfig = [
            name : cloudItem.name,
            externalId : cloudItem.name,
            cloud  : cloud,
            account  : cloud.account,
            resourcePool  : null,
            powerState : cloudItem.status == 'RUNNING' ? 'on' : 'off',
            computeServerType : serverType,
            serverType : 'vm',
            provision : false,
            status  : 'provisioned',
            internalId : cloudItem.id,
            uniqueId : cloudItem.id,
            osType : cloudItem.disks?.find { it.boot }?.guestOsFeatures?.find { it.type == 'WINDOWS' } ? 'windows' : 'linux',
        ]
        return vmConfig
    }

    def addMissingVirtualMachines(List addList,String projectId){
        log.info "Adding missing VMs: ${addList}"
        for(cloudItem in addList) {
            try {
                def zoneCode = cloudItem.zone.substring(cloudItem.zone.lastIndexOf('/') + 1)
                def vmConfig = buildVmConfig(cloudItem as Map)
                log.info "Adding VM: ${vmConfig}"
                ComputeServer add = new ComputeServer(vmConfig)
                add.setConfigProperty('googleZoneId', zoneCode)
                add.setConfigProperty('projectId', projectId)
                ComputeServer savedServer = morpheusContext.async.computeServer.create(add).blockingGet()
                if(!savedServer) {
                    log.error "Error in creating server ${add}"
                }
            }
            catch (Exception e) {
                log.error("Error adding VM: ${e.message}", e)
            }
        }
    }

    def removeMissingVirtualMachines(List<ComputeServerIdentityProjection> removeList){
        log.info "removeMissingVirtualMachines: ${cloud} ${removeList.size()}"
        morpheusContext.async.computeServer.remove(removeList).blockingGet()
    }

    void updateMatchedVirtualMachines(List<SyncTask.UpdateItem<ComputeServer, Map>> updateList){
        // code for update
    }


    def getVmsFromApi(String projectId){
        log.info("Trying to get response from GCP compute client")
        GcpClient client = new GcpClient(gcpDummyPlugin,cloud)
        def response = client.listVirtualMachines(projectId)
        def instances = []
        def scopedList = response?.getItems()
        scopedList?.each { _, zoneData ->
            zoneData?.instances?.each { instance ->
                instances << instance
            }
        }
        log.info("Instances: ${instances}")

        def cloudItems = [
                [
                        name: 'vm-01',
                        status: 'RUNNING',
                        id: 'vm-01-id',
                        zone: 'projects/sample-project/zones/us-central1-a',
                        disks: [
                                [
                                        boot: true,
                                        guestOsFeatures: [
                                                [ type: 'WINDOWS' ]
                                        ]
                                ]
                        ]
                ],
                [
                        name: 'vm-02',
                        status: 'TERMINATED',
                        id: 'vm-02-id',
                        zone: 'projects/sample-project/zones/us-central1-b',
                        disks: [
                                [
                                        boot: true,
                                        guestOsFeatures: [
                                                [ type: 'LINUX' ]
                                        ]
                                ]
                        ]
                ]
        ]

        ServiceResponse rtn = ServiceResponse.prepare()
        rtn.success = true
        rtn.results = instances
        rtn.data = instances

        return rtn
    }
}
