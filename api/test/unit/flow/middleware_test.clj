(ns flow.middleware-test
  (:require [flow.entity.user :as user]
            [flow.middleware :refer :all]
            [flow.specifications :as s]
            [flow.utils :as u]
            [flow.dummy :as d]
            [medley.core :as medley]
            [slingshot.slingshot :as slingshot]
            [clojure.test :refer :all]
            [slingshot.test :refer :all]))


(def request
  {:request-method :post
   :uri "/"
   :body "[\"^ \",\"~:query\",[\"^ \",\"~:users\",[\"^ \"]]]"
   :headers {"origin" "https://localhost:8080"
             "access-control-request-method" "POST"
             "content-type" "application/transit+json"
             "accept" "application/transit+json"}})

(def response
  {:status 200
   :headers {}
   :body {:users {}
          :authorisations {}
          :metadata {}
          :session {}}})

(def handler (constantly response))

(def user (first d/users))

(def authorisations d/authorisations)


(deftest test-wrap-access-control

  (testing "The wrapped handler returns the response with the correct access control applied to the entities."
    (let [response (-> response
                       (assoc-in [:body :users (:user/id user)] user)
                       (assoc-in [:body :authorisations (:authorisation/id (first authorisations))]
                                 (first authorisations))
                       (assoc-in [:body :authorisations (:authorisation/id (second authorisations))]
                                 (second authorisations)))
          handler' (wrap-access-control (constantly response))]
      (is (= {:status 200
              :headers {}
              :body {:users {}
                     :authorisations {}
                     :metadata {}
                     :session {}}}
             (handler' request))))
    (let [response (-> response
                       (assoc-in [:body :users (:user/id user)] user)
                       (assoc-in [:body :authorisations] (medley/index-by :authorisation/id authorisations))
                       (assoc-in [:body :session :current-user] user))
          handler' (wrap-access-control (constantly response))]
      (is (= {:status 200
              :headers {}
              :body {:users {(:user/id user) user}
                     :authorisations {}
                     :metadata {}
                     :session {:current-user user}}}
             (handler' request))))
    (let [current-user (assoc user :user/roles #{:customer :admin})
          response (-> response
                       (assoc-in [:body :users (:user/id user)] user)
                       (assoc-in [:body :authorisations] (medley/index-by :authorisation/id authorisations))
                       (assoc-in [:body :session :current-user] current-user))
          handler' (wrap-access-control (constantly response))]
      (is (= {:status 200
              :headers {}
              :body {:users {(:user/id user) user}
                     :authorisations (medley/index-by :authorisation/id authorisations)
                     :metadata {}
                     :session {:current-user current-user}}}
             (handler' request))))))


(deftest test-wrap-current-user

  (testing "The wrapped handler throws an exception when the current user cannot be fetched."
    (let [handler' (wrap-current-user (constantly response))
          request (assoc-in request [:body-params :session :current-user-id] (:user/id user))]
      (with-redefs [user/fetch (constantly nil)]
        (is (thrown+? [:type :flow/internal-error]
                      (handler' request))))))

  (testing "The wrapped handler returns a response with the current user removed and the current
            user id added to the body session no matter what the current user is."
    (with-redefs [user/fetch (constantly user)]
      (let [response (assoc-in response [:body :session] {:current-user nil})
            request (assoc-in request [:body-params :session :current-user-id] (:user/id user))
            handler' (wrap-current-user (constantly response))]
        (is (= {:status 200
                :headers {}
                :body {:users {}
                       :authorisations {}
                       :metadata {}
                       :session {:current-user-id nil}}}
               (handler' request))))
      (let [response (assoc-in response [:body :session] {:current-user {}})
            request (assoc-in request [:body-params :session :current-user-id] (:user/id user))
            handler' (wrap-current-user (constantly response))]
        (is (= {:status 200
                :headers {}
                :body {:users {}
                       :authorisations {}
                       :metadata {}
                       :session {:current-user-id nil}}}
               (handler' request))))
      (let [response (assoc-in response [:body :session] {:current-user user})
            request (assoc-in request [:body-params :session :current-user-id] (:user/id user))
            handler' (wrap-current-user (constantly response))]
        (is (= {:status 200
                :headers {}
                :body {:users {}
                       :authorisations {}
                       :metadata {}
                       :session {:current-user-id (:user/id user)}}}
               (handler' request)))))))


(deftest test-wrap-metadata

  (testing "The wrapped handler returns a response with irrelevant keys stripped from metadata."
    (let [handler' (wrap-metadata (constantly (assoc-in response [:body :metadata] {:hello "world"})))]
      (is (= {:status 200
              :headers {}
              :body {:users {}
                     :authorisations {}
                     :metadata {}
                     :session {}}}
             (handler' request)))))

  (testing "The wrapped handler returns a response with the ID resolution passed through with metadata."
    (let [handler' (wrap-metadata (constantly
                                   (assoc-in
                                    response
                                    [:body :metadata]
                                    {:id-resolution {"some-id" "some-other-id"}})))]
      (is (= {:status 200
              :headers {}
              :body {:users {}
                     :authorisations {}
                     :metadata {:id-resolution {"some-id" "some-other-id"}}
                     :session {}}}
             (handler' request)))))

  (testing "The wrapped handler returns a response with the relevant pagination fields passed through with metadata."
    (let [handler' (wrap-metadata (constantly
                                   (assoc-in
                                    response
                                    [:body :metadata]
                                    {:users {:next-offset 1
                                             :exhausted? true
                                             :hi :there}
                                     :authorisations {:limit 1
                                                      :exhausted? false}})))]
      (is (= {:status 200
              :headers {}
              :body {:users {}
                     :authorisations {}
                     :metadata {:users {:next-offset 1
                                        :exhausted? true}
                                :authorisations {:exhausted? false}}
                     :session {}}}
             (handler' request))))))


(deftest test-wrap-session

  (testing "The wrapped handler returns a response with a session when the body includes a non empty session."
    (let [handler' (wrap-session (constantly (assoc-in response [:body :session] {:hello "world"})))]
      (is (= {:status 200
              :headers {}
              :body {:users {}
                     :authorisations {}
                     :metadata {}
                     :session {:hello "world"}}
              :session {:hello "world"}}
             (handler' request)))))

  (testing "The wrapped handler returns a response with no session when the body includes an empty session."
    (let [handler' (wrap-session (constantly (assoc-in response [:body :session] {})))]
      (is (= {:status 200
              :headers {}
              :body {:users {}
                     :authorisations {}
                     :metadata {}
                     :session {}}
              :session {}}
             (handler' request)))))

  (testing "The wrapped handler throws an exception when body includes no session."
    (let [handler' (wrap-session (constantly (assoc-in response [:body :session] nil)))]
      (is (thrown+? [:type :flow/internal-error] (handler' request))))
    (let [handler' (wrap-session (constantly (update response :body dissoc :session)))]
      (is (thrown+? [:type :flow/internal-error] (handler' request))))))


(deftest test-wrap-session-persistence

  (testing "The wrapped handler returns a response with a cookie header when the session is non empty."
    (let [handler' (wrap-session-persistence (constantly (assoc response :session {:hello "world"})))]
      (is (some? (get-in (handler' request) [:headers "Set-Cookie"])))))

  (testing "The wrapped handler returns a response with a cookie header when the session is empty."
    (let [handler' (wrap-session-persistence (constantly (assoc response :session {})))]
      (is (some? (get-in (handler' request) [:headers "Set-Cookie"])))))

  (testing "The wrapped handler returns a response without a cookie header when the session is nil."
    (let [handler' (wrap-session-persistence (constantly (assoc response :session nil)))]
      (is (nil? (get-in (handler' request) [:headers "Set-Cookie"]))))))


(deftest test-wrap-content-validation

  (testing "The wrapped handler throws an exception when the request's content is invalid."
    (let [handler' (wrap-content-validation handler)]
      (is (thrown+? [:type :flow/unsupported-request]
                    (handler' (assoc request :body-params {:hello "world"}))))
      (is (thrown+? [:type :flow/unsupported-request]
                    (handler' (assoc request :body-params ""))))
      (is (thrown+? [:type :flow/unsupported-request]
                    (handler' (assoc request :body-params {:command {}
                                                           :query {}
                                                           :metadata {}
                                                           :session {}
                                                           :hello "world"}))))
      (is (thrown+? [:type :flow/unsupported-request]
                    (handler' (assoc request :body-params {:command {}
                                                           :query {:user {:user/id 1}}
                                                           :metadata {}
                                                           :session {}}))))
      (is (thrown+? [:type :flow/unsupported-request]
                    (handler' (assoc request :body-params {:command {}
                                                           :query {:users {}}
                                                           :metadata {:users {:limit "hi"}}
                                                           :session {}}))))))

  (testing "The wrapped handler throws an exception when the response's content is invalid."
    (let [handler' (wrap-content-validation handler)]
      (is (thrown+? [:type :flow/internal-error]
                    (handler' (assoc request :body-params {:command {}
                                                           :query {:users {}}
                                                           :metadata {}
                                                           :session {}}))))))

  (testing "The wrapped handler returns the response when both request and response content are valid."
    (let [handler' (wrap-content-validation handler)]
      (with-redefs [u/validate (constantly (:body response))]
        (is (= response
               (handler' (assoc request :body-params {:command {}
                                                      :query {:users {}}
                                                      :metadata {}
                                                      :session {}}))))))))


(deftest test-wrap-content-type

  (testing "The wrapped handler throws an exception when the request's content type header isn't
            transit."
    (let [handler' (wrap-content-type handler)]
      (is (thrown+? [:type :flow/unsupported-request]
                    (handler' (assoc-in request [:headers "content-type"] "application/json"))))
      (is (thrown+? [:type :flow/unsupported-request]
                    (handler' (update request :headers dissoc "content-type"))))))

  (testing "The wrapped handler throws an exception when the request's body cannot be decoded using
            the content type."
    (let [handler' (wrap-content-type handler)]
      (is (thrown+? [:type :flow/unsupported-request]
                    (handler' (assoc-in request [:headers "content-type"] "application/json"))))
      (is (thrown+? [:type :flow/unsupported-request]
                    (handler' (assoc request :body "hello world"))))))

  (testing "The wrapped handler returns the response body encoded with request's accept header."
    (let [handler' (wrap-content-type handler)]
      (is (= "[\"^ \",\"~:users\",[\"^ \"],\"~:authorisations\",[\"^ \"],\"~:metadata\",[\"^ \"],\"~:session\",[\"^ \"]]"
             (-> (handler' request) (:body) (slurp))))))

  (testing "The wrapped handler returns the response headers with content type equivalent to the
            request's accept header."
    (let [handler' (wrap-content-type handler)]
      (is (= {"Content-Type" "application/transit+json; charset=utf-8"}
             (:headers (handler' request)))))
    (let [handler' (wrap-content-type handler)]
      (is (= {"Content-Type" "application/json; charset=utf-8"}
             (:headers (handler' (assoc-in request [:headers "accept"] "application/json")))))))

  (testing "The wrapped handler returns the response body as is if it cannot be encoded with
            request's accept header."
    (let [handler' (wrap-content-type (constantly {:status 200 :headers {} :body "hello-world"}))]
      (is (= "hello-world"
             (:body (handler' request))))))

  (testing "The wrapped handler returns the response with no content type header if it cannot
            encode the response body with request's accept header."
    (let [handler' (wrap-content-type (constantly {:status 200 :headers {} :body "hello-world"}))]
      (is (= {}
             (:headers (handler' request)))))))


(deftest test-wrap-request-path

  (testing "The wrapped handler throws an exception when the request path isn't the root."
    (let [handler' (wrap-request-path handler)]
      (is (thrown+? [:type :flow/unsupported-request] (handler' (assoc request :uri "hello"))))
      (is (thrown+? [:type :flow/unsupported-request] (handler' (assoc request :uri "world"))))))

  (testing "The wrapped handler returns the response when the request path is the root."
    (let [handler' (wrap-request-path handler)]
      (is (= response (handler' request)))
      (is (= response (handler' (assoc request :uri "")))))))


(deftest test-wrap-request-method

  (testing "The wrapped handler throws an exception when the request method isn't POST."
    (let [handler' (wrap-request-method handler)]
      (is (thrown+? [:type :flow/unsupported-request] (handler' (assoc request :request-method :get))))
      (is (thrown+? [:type :flow/unsupported-request] (handler' (assoc request :request-method :options))))
      (is (thrown+? [:type :flow/unsupported-request] (handler' (assoc request :request-method :head))))
      (is (thrown+? [:type :flow/unsupported-request] (handler' (assoc request :request-method :patch))))
      (is (thrown+? [:type :flow/unsupported-request] (handler' (assoc request :request-method :delete))))))

  (testing "The wrapped handler returns the response when the request method is POST."
    (let [handler' (wrap-request-method handler)]
      (is (= response (handler' request))))))


(deftest test-wrap-exception

  (testing "The wrapped handler returns a 400 response when an unusable request exception is thrown."
    (let [handler' (wrap-exception
                    (fn [_]
                      (slingshot/throw+ {:type :flow/unsupported-request :message "Hello World."})))]
      (is (= {:body "{\"error\": \"Hello World.\"}",
              :headers {"Content-Type" "application/json; charset=utf-8"},
              :status 400}
             (handler' request)))))

  (testing "The wrapped handler returns a 500 response when an internal error exception is thrown."
    (let [handler' (wrap-exception
                    (fn [_]
                      (slingshot/throw+ {:type :flow/internal-error :message "Hello World."})))]
      (is (= {:body "{\"error\": \"Hello World.\"}",
              :headers {"Content-Type" "application/json; charset=utf-8"},
              :status 500}
             (handler' request)))))

  (testing "The wrapped handler returns a 500 response when an external error exception is thrown."
    (let [handler' (wrap-exception
                    (fn [_] (slingshot/throw+ {:type :flow/external-error :message "Hello World."})))]
      (is (= {:body "{\"error\": \"External error detected.\"}",
              :headers {"Content-Type" "application/json; charset=utf-8"},
              :status 500}
             (handler' request)))))

  (testing "The wrapped handler returns a 500 response when any other exception is thrown."
    (let [handler' (wrap-exception (fn [_] (slingshot/throw+ {:type :hello-world :message "Hello World."})))]
      (is (= {:body "{\"error\": \"Unspecified error detected.\"}"
              :headers {"Content-Type" "application/json; charset=utf-8"}
              :status 500}
             (handler' request))))
    (let [handler' (wrap-exception (fn [_] (throw (Exception. "hello world"))))]
      (is (= {:body "{\"error\": \"Unspecified error detected.\"}"
              :headers {"Content-Type" "application/json; charset=utf-8"}
              :status 500}
             (handler' request)))))

  (testing "The wrapped handler returns the response when no exceptions are thrown."
    (let [handler' (wrap-exception handler)]
      (is (= response (handler' request))))))


(deftest test-wrap-cors

  (testing "The wrapped handler returns a successful preflight response with CORS headers when the
            request method is OPTIONS."
    (let [handler' (wrap-cors handler)]
      (is (= {:body "preflight complete",
              :headers {"Access-Control-Allow-Credentials" "true",
                        "Access-Control-Allow-Headers" "Content-Type",
                        "Access-Control-Allow-Methods" "OPTIONS, POST",
                        "Access-Control-Allow-Origin" "https://localhost:8080"},
              :status 200}
             (handler' (assoc request :request-method :options))))))

  (testing "The wrapped handler returns a non-preflight response, and no CORS headers when the request
            method is OPTIONS, but origin isn't allowed."
    (let [handler' (wrap-cors handler)]
      (is (= response
             (handler' (-> request
                           (assoc :request-method :options)
                           (assoc-in [:headers "origin"] "hello-world")))))))

  (testing "The wrapped handler returns the response without CORS headers when the request method
            isn't one of methods with access control."
    (let [handler' (wrap-cors handler)]
      (is (= response
             (handler' (assoc request :request-method :get))))
      (is (= response
             (handler' (assoc request :request-method :head))))))

  (testing "The wrapped handler returns the response with CORS headers when the request method has
            access control."
    (let [handler' (wrap-cors handler)]
      (is (= {"Access-Control-Allow-Credentials" "true"
              "Access-Control-Allow-Methods" "OPTIONS, POST"
              "Access-Control-Allow-Origin" "https://localhost:8080"}
             (:headers (handler' request)))))))
