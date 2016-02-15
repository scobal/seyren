/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.seyren.core.service.checker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ohad on 7/19/15.
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class JenkinsJobStatus {

    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public int getJobStatus(){
        if (result.equalsIgnoreCase("ERROR")){
            return -1;
        }else if (result.equalsIgnoreCase("UNSTABLE")){
            return 0;
        }else{
            return 1;
        }
    }
}
