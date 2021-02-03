(ns flow.email
  (:require [ses-mailer.core :as mailer]))


(defn send-email
  [email-address subject html-body text-body]
  (mailer/send-email
   {}
   "Flow <noreply@flow.keigo.io>"
   email-address
   subject
   {:html-body html-body
    :text-body text-body}))
