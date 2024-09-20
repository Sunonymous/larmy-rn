(ns example.app
  (:require [example.events]
            [example.subs]
            [example.widgets :refer [button]]
            [clojure.math :refer [floor]]
            [expo.root :as expo-root]
            ["expo-status-bar" :refer [StatusBar]]
            [re-frame.core :as rf]
            ["react-native" :as rn]
            [reagent.core :as r]
            ["@react-native-community/slider$default" :as Slider]
            ["@react-navigation/native" :as rnn]
            ["@react-navigation/native-stack" :as rnn-stack]))

(defonce Stack (rnn-stack/createNativeStackNavigator))

(defonce time-values
  ["5sec"
  "15sec"
  "30sec"
  "45sec"
  "1min"
  "5min"
  "15min"
  "30min"
  "45min"
  "1hr"
  "4hr"
  "8hr"
  "16hr"
  "1day"
  "3day"
  "5day"
  "1week"
  "2week"
  "3week"
  "1month"])

(def time-sections ["seconds" "minutes" "hours" "days" "weeks" "month"])

(defn screen-wrapper []
  (let [window-properties (rn/useWindowDimensions)
        clj-dimensions    (js->clj window-properties)]
    [:> rn/View
     [:> rn/Text
      (str (.toFixed (clj-dimensions "width") 2) " x " (.toFixed (clj-dimensions "height") 2))]]))

(defn home [^js props]
  (r/with-let [larmy-voice*  (r/atom "Hello.")
               slider-val*   (r/atom 0)
               tap-enabled?  (rf/subscribe [:counter-tappable?])]
    (fn [^js props]
      [:> rn/View {:style {:flex 1
                           :padding-vertical 50
                           :justify-content :space-between
                           :align-items :center
                           :background-color :white}}
       [button {:style {:align-self "flex-end"}
                :on-press #(-> props .-navigation (.navigate "About"))}
        "About"]
       [:> rn/View {:style {:align-items :center}}
        [:f> screen-wrapper] ;; TODO get this into a component which will update DB upon screen size change
        [:> rn/Text {:style {:font-weight   :bold
                             :font-size     72
                             :color         :blue
                             :margin-bottom 20}}
            ;; @slider-val* ;; TODO put this back or replace later once the other component is created
         (if (pos? @slider-val*) "1 " "") " " (get time-sections (floor @slider-val*) "")]]
       [:> rn/View {:style {:align-items :center}}
        [:> Slider {:minimum-value    -1
                    :maximum-value    (dec (count time-sections))
                    :value            -1 ;; start in the "inactive state"
                    :on-value-change #(reset! slider-val* %)
                    :style            {:width 300 :height 40}
                    :minimumTrackTintColor :blue
                    :maximumTrackTintColor :gray}]

        [button {:on-press #(rf/dispatch [:inc-counter])
                 :disabled? (not @tap-enabled?)
                 :style {:background-color :blue}}
         "Notify"]]
       [:> rn/View
        [:> StatusBar {:style "auto"}]]])))

(defn- about
  []
  (r/with-let [counter (rf/subscribe [:get-counter])]
    [:> rn/View {:style {:flex 1
                         :padding-vertical 50
                         :padding-horizontal 20
                         :justify-content :space-between
                         :align-items :flex-start
                         :background-color :white}}
     [:> rn/View {:style {:align-items :flex-start}}
      [:> rn/Text {:style {:font-weight   :bold
                           :font-size     42
                           :color         :blue
                           :margin-bottom 20}}
       "About Example App"]
      [:> rn/Text {:style {:font-weight   :bold
                           :font-size     20
                           :color         :blue
                           :margin-bottom 20}}
       (str "Counter is at: " @counter)]
      [:> rn/Text {:style {:font-weight :normal
                           :font-size   15
                           :color       :blue}}
       "Built with React Native, Expo, Reagent, re-frame, and React Navigation"]]
     [:> StatusBar {:style "auto"}]]))

(defn root []
  ;; The save and restore of the navigation root state is for development time bliss
  (r/with-let [!root-state (rf/subscribe [:navigation/root-state])
               save-root-state! (fn [^js state]
                                  (rf/dispatch [:navigation/set-root-state state]))
               add-listener! (fn [^js navigation-ref]
                               (when navigation-ref
                                 (.addListener navigation-ref "state" save-root-state!)))]
    [:> rnn/NavigationContainer {:ref add-listener!
                                 :initialState (when @!root-state (-> @!root-state .-data .-state))}
     [:> Stack.Navigator
      [:> Stack.Screen {:name "Home"
                        :component (fn [props] (r/as-element [home props]))
                        :options {:title "Larmy"}}]
      [:> Stack.Screen {:name "About"
                        :component (fn [props] (r/as-element [about props]))
                        :options {:title "About"}}]]]))

(defn start
  {:dev/after-load true}
  []
  (expo-root/render-root (r/as-element [root])))

(defn init []
  (rf/dispatch-sync [:initialize-db])
  (start))