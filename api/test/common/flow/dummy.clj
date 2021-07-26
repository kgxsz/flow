(ns flow.dummy)


(def users
  [{:user/id #uuid "19f3c785-cf5f-530b-841d-6161400e6793"
    :user/email-address "j.mcjohnson@gmail.com"
    :user/name "Johnson"
    :user/roles #{:customer}
    :user/created-at #inst "2021-04-02T19:58:19.213-00:00"
    :user/deleted-at nil}])


(def authorisations
  [{:authorisation/id #uuid "31f3c785-0f5f-530b-841d-7761400e6793"
    :user/id #uuid "19f3c785-cf5f-530b-841d-6161400e6793"
    :authorisation/phrase "amount-addition-harbor",
    :authorisation/created-at #inst "2021-04-03T11:21:46.894-00:00",
    :authorisation/granted-at nil}
   {:authorisation/id #uuid "22f3c785-0f5f-530b-841d-7761400e6793"
    :user/id #uuid "00f3c785-cf5f-530b-841d-6161400e6793"
    :authorisation/phrase "concrete-tree-bridge",
    :authorisation/created-at #inst "2021-04-03T11:21:46.894-00:00",
    :authorisation/granted-at nil}])
