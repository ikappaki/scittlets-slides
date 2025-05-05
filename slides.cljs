(ns slides 
  (:require
    [reagent.core :as r]
    [reagent.dom :as rdom]
    [scittlets.reagent.mermaid :refer [mermaid+]]))

(def graphs
  {:hello "%%{init: {'theme':'dark'}}%%

sequenceDiagram
    Alice ->> Bob: Hello Bob, how are you?
    Bob-->>John: How about you John?
    Bob--x Alice: I am good thanks!
    Bob-x John: I am good thanks!
    Note right of John: Bob thinks a long<br/>long time, so long<br/>that the text does<br/>not fit on a row.

    Bob-->Alice: Checking with John...
    Alice->John: Yes... John, how are you?"

   :journey "%%{init: {'theme':'dark'}}%%

journey
    title My working day
    section Go to work
      Make tea: 5: Me
      Go upstairs: 3: Me
      Do work: 1: Me, Cat
    section Go home
      Go downstairs: 5: Me
      Sit down: 5: Me"   })

(defn slides-counter-header [state]
  (when-let [get-slide-count (:get-slide-count-fn @state)]
    (let [{:keys [slide]} @state
          scount (get-slide-count)
          slide (inc (mod slide scount))]
      [:header {:style {:font-size "0.4em"}} (str slide "/" scount)]
      ))
  )

(defn slides [state]
  [:<>
   ;; your slides start here
   ;; each slide is a :section
   ;; you can add whatever hiccup you like

   [:section
    [slides-counter-header state]
    [:h1 "Hello 🧜‍♀️"]
    [mermaid+ (:hello graphs)]
    [:footer
     [:small
      [:a {:href "https://github.com/chr15m/scittle-tiny-slides"
           :target "_BLANK"}
       "Made with Scittle Tiny Slides"]]]]

   [:section
    [slides-counter-header state]
    [:h1 "Journey 🐈"]
    [mermaid+ (:journey graphs)]]

   [:section
    [slides-counter-header state]
    [:h1 "Links"]
    [:ul 
     [:li [:span
           "Slides: "
           [:a {:href "https://github.com/ikappaki/scittlets-slides"} "https://github.com/ikappaki/scittlets-slides"]]]
     [:li [:span
           "Scittlets: "
           [:a {:href "https://ikappaki.github.io/scittlets/"} "https://ikappaki.github.io/scittlets/"]]]
     [:li [:span
           "Scittle: "
           [:a {:href "https://github.com/babashka/scittle"} "https://github.com/babashka/scittle"]]]]]
   
   ])

; *** implementation details *** ;

(defonce state (r/atom nil)) ; re-initialized below

(defn get-slide-count []
  (aget
    (js/document.querySelectorAll "section")
    "length"))

(defn move-slide! [state ev dir-fn]
  (.preventDefault ev)
  (swap! state update :slide dir-fn))

(defn clickable? [ev]
  (let [tag-name (.toLowerCase (aget ev "target" "tagName"))]
    (contains? #{"button" "label" "select"
                 "textarea" "input" "a"
                 "details" "summary"}
               tag-name)))

(defn keydown
  [ev]
  (when (not (clickable? ev))
    (let [k (aget ev "key")]
      (cond
        (contains? #{"ArrowLeft" "ArrowUp" "PageUp"} k)
        (move-slide! state ev dec)
        (contains? #{"ArrowRight" "ArrowDown" "PageDown" "Enter" " "} k)
        (move-slide! state ev inc)
        (contains? #{"Escape" "Home" "h"} k)
        (swap! state assoc :slide 0)
        (contains? #{"End"} k)
        (swap! state assoc :slide (dec (get-slide-count)))))))

(defn component:show-slide [state]
  [:style (str "section:nth-child("
               (inc (mod (:slide @state) (get-slide-count)))
               ") { display: block; }")])

(defn component:touch-ui [state]
  [:div#touch-ui
   {:style {:opacity
            (if (:touch-ui @state) 0 1)}}
   [:div {:on-click #(move-slide! state % dec)} "⟪"]
   [:div {:on-click #(move-slide! state % inc)} "⟫"]])

(defn component:slide-viewer [state]
  [:<>
   [:main {:on-click
           #(when (not (clickable? %))
              (swap! state update :touch-ui not))}
    [slides state get-slide-count]]
   [component:show-slide state]
   [component:touch-ui state]])

(rdom/render
  [component:slide-viewer state]
  (.getElementById js/document "app"))

(defonce setup
  (do
    (aset js/window "onkeydown" #(keydown %))
    ; trigger a second render so we get the sections count
    (swap! state assoc :slide 0 :touch-ui true :get-slide-count-fn get-slide-count)))
