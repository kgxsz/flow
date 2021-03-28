(ns flow.specifications
  (:require [clojure.spec.alpha :as s]))


;; TODO - request body params is either query or command
;; Has predictable structure by the method. But it's not
;; possible to know if it's query or command, so relies
;; on mutually exclusive method namings between query and
;; command, this is not suitable. Time to unify and use
;; separate query and command maps in the same body params.
(s/def :request/body-params #(and (map? %) (not (empty? %))))

;; {:command {:add-user {:user/id "x" :user/name "John"}}
;;  :query {:user {:user/id "x"}
;;          :users {}}}
;; Metadata? For paginations and stuff like that?


;; TODO - response body has entities, and metadata, that's it.
(s/def :response/body #(and (map? %) (not (empty? %))))

;; {:users {"y" {:user/id "y" :user/name "John"}
;;          "z" {:user/id "z" :user/name "Yoko"}
;;  :metadata {:current-user-id "z"
;;             :id-resolution {"x" "y"}}}}
