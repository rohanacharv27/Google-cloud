/*
* Copyright 2022 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.morpheusdata.GCP_dummy

import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import groovy.util.logging.Slf4j

@Slf4j
class GcpDummyPlugin extends Plugin {

    @Override
    String getCode() {
        return 'GcpDummy'
    }

    @Override
    void initialize() {
        this.setName("GcpDummy")

        this.registerProvider(new GcpDummyCloudProvider(this,this.morpheus))
        this.registerProvider(new GcpDummyProvisionProvider(this,this.morpheus))

    }

    /**
     * Called when a plugin is being removed from the plugin manager (aka Uninstalled)
     */
    @Override
    void onDestroy() {
        //nothing to do for now
    }

    MorpheusContext getMorpheusContext() {
        return morpheus
    }

    def ClientEmail = "svcaccount@shanjhunaa-panneerselvam.iam.gserviceaccount.com"

    def PrivateKey = "-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDI8od70gTTllxo\\n0R6Ibfq0ySwT4BLg3hVTfK0H/fFRYe2zCZs6OfjUeDuLHjn+FCnm2nLCk4UDyJ7m\\n9krP58zG+mnjtq8mOWkvZkp4m4vasSQOZW4Yyt1Nri6tAaVxEKzi+sMeIHvhC7X+\\naxp4t1Tl7iJCafZXxtnDarYAD3HGBSmIubnzmQGtclwU+gNtOFozeo1rgZE/p3sS\\nrGKJUQNeZfk6E3jPbnzKfvD09QC7/wTas7u75DItfbN1/gLhWOlXEAExS7zK5T/7\\njY/qxKFBlUSqSM0gSK8YLqHl6LR8BNeTvWThxotTaL9PZmesGFero24elVaIsCl5\\nXjuTQae3AgMBAAECggEAJcOyo5oGLy+QYWB/oPsuRSCTiI6xbZI/JsgFCr32Rqml\\nTuLsycYQVfncktoU4wOuFkUSkS/BLUNcXaKGz8jL6s8v7EwuYXwjoPVT5JOy9mAZ\\nGx2D0eP6MDmeawQmr/eGzkd7OOO49EPLqas+N9aYfXfB94JLehaUYU3iM2OzqZeE\\ncAmDQ4X25GtcwI2fH3Xvo7zAQWEYweZ0tRZdUOqSYR9Ap6E13E8SmvTbwXUyqi3U\\n1EiOhtSAMhWMrIFH8DOlKVkPgNc4XuwsY84yZjIecv1dKXckqRLq3Vu8CwTjrdWt\\nhMp5UStNuP4zTQzQix8QNB/+hT6wgZJr0hMudQS+JQKBgQDs+q9WetwTXAqTWRG7\\nFG2sGLn5g/0FulcwPDFdbOg1Y5FVrJfxs15JS0IJSRbkWy/Vw3p8FL3Ee9PlvCkr\\nas15rWeH06s7kNU5qcqCFeRUNv8GiYcAu0sOwioUp6Xtb9dd8kxl2O2eaSGBVUDT\\n1TxnPIgF8KUo0FLmEIoVdwLSWwKBgQDZE3tsFhjyzLKOFy1uWcnGG5NWzcSfaYT9\\npBXkVl785fBW9kC0wNwK7IGNx43rVpVGwzkQxkElsUthvvywG1Z6gJesIqz7W3P2\\npdiqkwyfSSy+oAnuOmko0NhtTTTuNmUsAlzSb+XDNCEjCM7sIB01R+RNdIfPrs3+\\nYdWfuveG1QKBgDEwG2LE35c5HIzXCQIezGDTRQ7QYZ1lvyEo7UoqB8zFHO5+g49M\\nnr5x24QX4qOFddlV2L+Wgokc9q+Rra3wXjPqVTnxKnKTrl5oqrrzllAQlkCiHR5+\\nwaaPkLJ21c8Bzt8WIttk+nVN9wFkU69kbEQ1YTpyZ6WgEATm2/J2oGRPAoGANFja\\nXopFoedy94jnT8EfQ0LLGRet3rNXesLi9JlPdrUMbCweFnibl5bI9yO0OZg7j0Ds\\ntfrZIvnKL9wam208QXZZHQ2PoV3AbS45PYkQdijzPVTikCxNx6X1SwZfSUKcyywW\\nrjiFs7kSdJxfKdnksUCQ8Yy0Y4TH8Bwvo8E2h7ECgYEAjSpZkgyjx4A8UIDPUcsk\\nP59B26n187JThLWuA70HlnjL1LQufbKXFOR/LaB9Pkw6d7cJlOnO90SRmPtyaLuI\\nto6Zi4ai84mM/672SWGjyCuHBVGldF3KpVzM2eK5UNEOLjE3bCU4WihzBuWwWTq5\\nllHKLOrJrb6sZtYIum5A3lg=\\n-----END PRIVATE KEY-----\\n"
}
