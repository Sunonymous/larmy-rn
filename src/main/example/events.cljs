(ns example.events
  (:require
   [re-frame.core :as rf]
   [example.db :as db :refer [app-db]]))


(rf/reg-event-db
 :initialize-db
 (fn [_ _]
   app-db))

(rf/reg-event-db
 :set-alarm
 (fn [db [_ alarm-details]]
   db))

(rf/reg-event-db
 :navigation/set-root-state
 (fn [db [_ navigation-root-state]]
   (assoc-in db [:navigation :root-state] navigation-root-state)))
