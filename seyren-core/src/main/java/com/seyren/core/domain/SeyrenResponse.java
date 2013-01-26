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
package com.seyren.core.domain;

import java.util.List;

public class SeyrenResponse<T> {
    
    private List<T> values;
    
    private int items;
    
    private int start;
    
    private int total;
    
    public List<T> getValues() {
        return values;
    }
    
    public void setValues(List<T> values) {
        this.values = values;
    }
    
    public SeyrenResponse<T> withValues(List<T> values) {
        setValues(values);
        return this;
    }
    
    public int getItems() {
        return items;
    }
    
    public void setItems(int items) {
        this.items = items;
    }
    
    public SeyrenResponse<T> withItems(int items) {
        setItems(items);
        return this;
    }
    
    public int getStart() {
        return start;
    }
    
    public void setStart(int start) {
        this.start = start;
    }
    
    public SeyrenResponse<T> withStart(int start) {
        setStart(start);
        return this;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public SeyrenResponse<T> withTotal(int total) {
        setTotal(total);
        return this;
    }
    
}
