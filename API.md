# Seyren API Document v1 

## Alerts API

### Get check alerts

* **URL** /api/checks/{checkId}/alerts

* **Method** GET

* **URL Params**

| Parameter      | Required         | Description            | Type      |
|----------------|------------------|------------------------|-----------|
| checkId        |  true            | Check id               | String    |

* **Query Params**

| Parameter | Required   | Description                   | Type    |
|-----------|------------|-------------------------------|---------|
| start     |  false     | Index of the first result     | int     |
| items     |  false     | Number of resultats to return | int     |

* **Body**

None

* **Response**

GET /api/checks/540571f4f562fe8c51873ad1/alerts?items=10&start=0

```json
{  
   "values":[  
      {  
         "id":"544c012636623111967d9094",
         "checkId":"540571f4f562fe8c51873ad1",
         "value":606.0,
         "target":"server1.filecount.whisper.files",
         "warn":700,
         "error":1000,
         "fromType":"WARN",
         "toType":"OK",
         "timestamp":1414267174986,
         "targetHash":"�b�'8��i\u0017pN�_�om"
      },
      {  
         "id":"544c00ea36623111967d9093",
         "checkId":"540571f4f562fe8c51873ad1",
         "value":606.0,
         "target":"server1.filecount.whisper.files",
         "warn":100,
         "error":800,
         "fromType":"WARN",
         "toType":"WARN",
         "timestamp":1414267114984,
         "targetHash":"�b�'8��i\u0017pN�_�om"
      },
...
      {  
         "id":"544bff0b36623111967d908b",
         "checkId":"540571f4f562fe8c51873ad1",
         "value":606.0,
         "target":"server1.filecount.whisper.files",
         "warn":100,
         "error":800,
         "fromType":"WARN",
         "toType":"WARN",
         "timestamp":1414266635230,
         "targetHash":"�b�'8��i\u0017pN�_�om"
      }
   ],
   "items":10,
   "start":0,
   "total":95
}
```

### Delete check alerts

* **URL** /api/checks/{checkId}/alerts

* **Method** DELETE

* **URL Params**

| Parameter      | Required         | Description            | Type      |
|----------------|------------------|------------------------|-----------|
| checkId         |  true           | Check id               | String    |

* **Query Params**

| Parameter | Required   | Description            | Type    |
|-----------|------------|------------------------|---------|
| before     |  false     | Delete alerts before 'before' date for this check id | Date     |

* **Body**

  None

* **Response**

DELETE /api/checks/540571f4f562fe8c51873ad1/alerts?before=2014-10-26T13:06:28%2B01:00

Return '204 No Content'

### Get alerts

* **URL** /api/alerts

* **Method** GET

* **URL Params**

None

* **Query Params**

| Parameter | Required   | Description            | Type    |
|-----------|------------|------------------------|---------|
| start     |  false     | Index of the first result | int     |
| items     |  false     | Number of resultats to return | int |

* **Body**

  None

* **Response**

GET /api/alerts

```json
{  
   "values":[  
      {  
         "id":"544c012636623111967d9094",
         "checkId":"540571f4f562fe8c51873ad1",
         "value":606.0,
         "target":"server1.filecount.whisper.files",
         "warn":700,
         "error":1000,
         "fromType":"WARN",
         "toType":"OK",
         "timestamp":1414267174986,
         "targetHash":"�b�'8��i\u0017pN�_�om"
      },
      {  
         "id":"544c00ea36623111967d9093",
         "checkId":"540571f4f562fe8c51873ad1",
         "value":606.0,
         "target":"server1.filecount.whisper.files",
         "warn":100,
         "error":800,
         "fromType":"WARN",
         "toType":"WARN",
         "timestamp":1414267114984,
         "targetHash":"�b�'8��i\u0017pN�_�om"
      },
...
      {  
         "id":"544bff0b36623111967d908b",
         "checkId":"540571f4f562fe8c51873ad1",
         "value":606.0,
         "target":"server1.filecount.whisper.files",
         "warn":100,
         "error":800,
         "fromType":"WARN",
         "toType":"WARN",
         "timestamp":1414266635230,
         "targetHash":"�b�'8��i\u0017pN�_�om"
      }
   ],
   "items":10,
   "start":0,
   "total":95
}
```

## Charts API

### Get chart image for a given check

* **URL** /api/checks/{checkId}/image

* **Method** GET

* **URL Params**

| Parameter      | Required         | Description            | Type      |
|----------------|------------------|------------------------|-----------|
| checkId         |  true           | Check id               | String    |

* **Query Params**

| Parameter       | Required   | Description            | Type    |
|-----------------|------------|------------------------|---------|
| width           |  false     | Width of the return image | int (1200 by default) |
| height          |  false     | Height of the return image | int (350 by default) |
| from            |  false     | Specifies the beginning | String ("-24hours" by default) |
| to              |  true      | Specifies the end | String |
| hideThresholds  |  false     | Hide thresholds | boolean |
| hideLegend      |  false     | Hide legend | boolean |
| hideAxes        |  false     | Hide axes | boolean |

* **Body**

  None

* **Response**

GET /api/alerts

Return png image

### Get chart image for a given target

* **URL** /api/chart/{target}

* **Method** GET

* **URL Params**

| Parameter      | Required         | Description            | Type      |
|----------------|------------------|------------------------|-----------|
| target         |  true           | Metric name              | String    |

* **Query Params**

| Parameter       | Required   | Description            | Type    |
|-----------------|------------|------------------------|---------|
| width           |  false     | Width of the return image | int (1200 by default) |
| height          |  false     | Height of the return image | int (350 by default) |
| from            |  false     | Specifies the beginning | String ("-24hours" by default) |
| to              |  true      | Specifies the end | String |
| warn  |  false     | Warn level value | String |
| error  |  false     | Error level value | String |
| hideLegend      |  false     | Hide legend | boolean |
| hideAxes        |  false     | Hide axes | boolean |

* **Body**

  None

* **Response**

GET /api/alerts

Return png image

## Checks API

### Search checks

See [Checks resource javadoc](https://github.com/scobal/seyren/blob/master/seyren-api/src/main/java/com/seyren/api/jaxrs/ChecksResource.java#L63)

* **URL** /api/checks

* **Method** GET

* **URL Params**

None

* **Query Params**

| Parameter | Required   | Description            | Type      |
|-----------|------------|------------------------|-----------|
| state     |  false     | states checks          | AlertType |
| enabled   |  false     | enable/disable check   | boolean   |
| name      |  false     | ??                     | String    |
| fields    |  false     | Field name on which a regex will be applied        | String       |
| regexes   |  false     | Regexp                 | String       |

* **Body**

  None

* **Response**

GET /api/checks?enabled=true&state=ERROR&state=WARN&state=EXCEPTION&state=UNKNOWN

```json
{  
   "values":[  
      {  
         "id":"540571f4f562fe8c51873ad1",
         "name":"Test",
         "description":"test",
         "target":"server1.filecount.whisper.files",
         "from":null,
         "until":null,
         "warn":"100",
         "error":"1000",
         "enabled":true,
         "live":false,
         "state":"WARN",
         "lastCheck":1414425107940,
         "subscriptions":[  
            {  
               "id":"54481212873059b3ba063d4e",
               "target":"xxxx",
               "type":"SLACK",
               "su":true,
               "mo":true,
               "tu":true,
               "we":true,
               "th":true,
               "fr":true,
               "sa":true,
               "ignoreWarn":false,
               "ignoreError":false,
               "ignoreOk":false,
               "fromTime":"0000",
               "toTime":"2359",
               "enabled":true
            }
         ]
      }
   ],
   "items":0,
   "start":0,
   "total":1
}
```


### Create a check

* **URL** /api/checks

* **Method** POST

* **URL Params**

None

* **Query Params**

None

* **Body**

| Parameter | Required   | Description            | Type    |
|-----------|------------|------------------------|---------|
| name | true      | Name of the check | String |
| description | false      | Description of the check | String |
| target | true      | Name of the metric in graphite | String |
| warn | true      | Warn level |  String |
| error | true      | Error level | String |
| enabled | true      | Enable/Disable value | boolean |
| live | false      | Live value (pickle protocol) | boolean |
| from | false      | Specifies the beginning  | String |
| until | false      | Specifies the end | String |

* **Response**

POST /api/checks

```json
{  
   "name":"111",
   "description":"222",
   "target":"333",
   "warn":"666",
   "error":"777",
   "enabled":true,
   "live":false,
   "totalMetric":0,
   "from":"444",
   "until":"555"
}
```

Response '201 Created'

### Get a check

* **URL** /api/checks/{checkId}

* **Method** GET

* **URL Params**

| Parameter      | Required         | Description            | Type      |
|----------------|------------------|------------------------|-----------|
| checkId         |  true           | Check id              | String    |

* **Query Params**

None

* **Body**

None

* **Response**

GET /api/checks/540571f4f562fe8c51873ad1

```json
{  
   "id":"540571f4f562fe8c51873ad1",
   "name":"Test",
   "description":"test",
   "target":"server1.filecount.whisper.files",
   "from":null,
   "until":null,
   "warn":"100",
   "error":"1000",
   "enabled":true,
   "live":false,
   "state":"WARN",
   "lastCheck":1414444752519,
   "subscriptions":[  
      {  
         "id":"54481212873059b3ba063d4e",
         "target":"xxxx",
         "type":"SLACK",
         "su":true,
         "mo":true,
         "tu":true,
         "we":true,
         "th":true,
         "fr":true,
         "sa":true,
         "ignoreWarn":false,
         "ignoreError":false,
         "ignoreOk":false,
         "fromTime":"0000",
         "toTime":"2359",
         "enabled":true
      }
   ]
}
```

### Update a check

* **URL** /api/checks/{checkId}

* **Method** PUT

* **URL Params**

| Parameter      | Required         | Description            | Type      |
|----------------|------------------|------------------------|-----------|
| checkId         |  true           | Check id               | String    |

* **Query Params**

None

* **Body**

 Parameter | Required   | Description            | Type    |
|-----------|------------|------------------------|---------|
| name | true      | Name of the check | String |
| description | false      | Description of the check | String |
| target | true      | Name of the metric in graphite | String |
| warn | true      | Warn level |  String |
| error | true      | Error level | String |
| enabled | true      | Enable/Disable value | boolean |
| live | false      | Live value (pickle protocol) | boolean |
| from | false      | Specifies the beginning  | String |
| until | false      | Specifies the end | String |
| state| false | Specifies the state | String |

* **Response**

PUT /api/checks/540571f4f562fe8c51873ad1

```json
{  
   "id": "540571f4f562fe8c51873ad1",
   "name":"2222",
   "description":"2222",
   "target":"3333",
   "warn":"6666",
   "error":"7777",
   "enabled":true,
   "live":false,
   "from":"4444",
   "until":"5555",
   "state": "OK"
}
```

Return '200 OK'

### Delete a check

* **URL** /api/checks/{checkId}

* **Method** DELETE

* **URL Params**

| Parameter      | Required         | Description            | Type      |
|----------------|------------------|------------------------|-----------|
| checkId         |  true           | Check id               | String    |

* **Query Params**

None

* **Body**

None

* **Response**

DELETE /api/checks/540571f4f562fe8c51873ad1

Return '204 No Content'


## Config API

### Get Seyren configuration

* **URL** /config

* **Method** GET

* **URL Params**

  None

* **Query Params**

  None

* **Body**

  None

* **Response**

```json
{
  "baseUrl":"http://localhost:8080/seyren",
  "graphsEnabled":true,
  "graphiteCarbonPickleEnabled":false
}
```

## Metrics API

### Get metric count

* **URL** /api/metrics/{target}/total

* **Method** GET

* **URL Params**

| Parameter      | Required         | Description            | Type      |
|----------------|------------------|------------------------|-----------|
| target         |  true            | Return metric count    | String    |

* **Body**

  None

* **Response**

GET /metrics/path.metric.xxx/total

```json
{ "path.metric.xxx": 3 }
```

## Subscriptions API

### Create a subscription

* **URL** /checks/{checkId}/subscriptions

* **Method** POST

* **URL Params**

  None

* **Query Params**

| Parameter      | Required         | Description            | Type      |
|----------------|------------------|------------------------|-----------|
| checkId        |  true            | Check id               | String    |

* **Body**

  None

* **Response**

POST /api/checks/540571f4f562fe8c51873ad1/subscriptions

```json
{  
   "target":"test@gmail.com",
   "type":"EMAIL",
   "ignoreWarn":false,
   "ignoreError":false,
   "ignoreOk":false,
   "notifyOnWarn":true,
   "notifyOnError":true,
   "notifyOnOk":true,
   "fromTime":"0000",
   "toTime":"2359",
   "su":true,
   "mo":true,
   "tu":true,
   "we":true,
   "th":true,
   "fr":true,
   "sa":true,
   "enabled":true
}
```

Return '201 Created'

### Update a subscription

* **URL** /api/checks/{checkId}/subscriptions/{subscriptionId}

* **Method** PUT

* **URL Params**

  None

* **Query Params**

  None

* **Body**

  None

* **Response**

PUT /api/checks/540571f4f562fe8c51873ad1/subscriptions

```json
{  
   "target":"test@gmail.com",
   "type":"EMAIL",
   "ignoreWarn":false,
   "ignoreError":false,
   "ignoreOk":false,
   "notifyOnWarn":true,
   "notifyOnError":true,
   "notifyOnOk":true,
   "fromTime":"0000",
   "toTime":"2359",
   "su":true,
   "mo":true,
   "tu":true,
   "we":true,
   "th":true,
   "fr":true,
   "sa":true,
   "enabled":true
}
```

Return '204 No Content'

### Delete a subscription

* **URL** /api/checks/{checkId}/subscriptions/{subscriptionId}

* **Method** DELETE

* **URL Params**

| Parameter      | Required         | Description            | Type      |
|----------------|------------------|------------------------|-----------|
| checkId        |  true            | Check id               | String    |
| subscriptionId        |  true            | subscription id               | String    |

* **Query Params**

  None

* **Body**

  None

* **Response**

DELETE /api/checks/540571f4f562fe8c51873ad1/subscriptions/544eb9608730756ff45c52a5

Return '204 No Content'
 
### Test a subscription

* **URL** /api/checks/{checkId}/subscriptions/{subscriptionId}/test

* **Method** PUT

* **URL Params**

| Parameter      | Required         | Description            | Type      |
|----------------|------------------|------------------------|-----------|
| checkId        |  true            | Check id               | String    |
| subscriptionId        |  true            | Subscription id               | String    |

* **Query Params**

  None

* **Body**

  None

* **Response**

PUT /api/checks/540571f4f562fe8c51873ad1/subscriptions/54481212873059b3ba063d4e/test

Return '204 No Content'
