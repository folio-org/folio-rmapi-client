package org.folio.holdingsiq.service.util;

public class DataUtils {

  /**
  * The method for stubbing resource endpoint
  * @return String response body mock
  * @GET /{custid}/vendors/{vendorid}/packages/{packageid}/titles/{kbid}
  * */
  public static String getResourceBody() {
    return """
      {
        "titleId": 1157191,
        "titleName": "A Tale of Two Cities",
        "publisherName": "www.ebsco.com",
        "identifiersList": [
          {
            "id": "1",
            "source": "AtoZ",
            "subtype": 1,
            "type": 1
          }
        ],
        "subjectsList": [
          {
            "type": "BISAC",
            "subject": "PHILOSOPHY / Movements / Critical Theory"
          }
        ],
        "isTitleCustom": false,
        "alternateTitleList": [
          {
            "alternateTitle": "Boston Coll Law Rev",
            "titleType": "Abbreviated"
          }
        ],
        "pubType": "Journal",
        "customerResourcesList": [
          {
            "titleId": 1157191,
            "packageId": 1117849,
            "packageName": "Health & Wellness Resource Center (w/alt health module)",
            "packageType": "Complete",
            "proxy": {
              "id": "guest",
              "name": "EZ_proxy",
              "proxiedUrl": "http://ezproxy.myinstitute.edu/Login?url=http://search.ebscohost.com/Login.aspx?outhtype=ip,uid&profile=ehost&defaultdb=27h"
            }
          }
        ]
      }
      """;
  }
}
