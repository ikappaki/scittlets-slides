(ns slides-codemirror
  (:require
    [scittlets.reagent.codemirror :refer [EditorView+
                                          esm-import when-esm-modules-ready+
                                          esm-codemirror* esm-codemirror-view*]]))

(def esm-lang-mermaid*
  (esm-import "https://esm.sh/codemirror-lang-mermaid" mermaid))

(defn codemirror-mermaid+
  [TEXT INPUT*]
  (let [{:keys [basicSetup]} @esm-codemirror*
        {:keys [EditorView]} @esm-codemirror-view*
        {:keys [mermaid]} @esm-lang-mermaid*
        listener (-> EditorView
                     .-updateListener
                     (.of (fn [update]
                            (when (.-docChanged update)
                              (reset! INPUT* (-> update .-state .-doc (.toString)))))))]
    (fn [_ _]
      [:div {:style {:font-size "0.32em"
                     :display "inline-block"}}
       [EditorView+ {:doc TEXT
                     :extensions [basicSetup (mermaid) listener]}]])))

(defn codemirror+
  [text input*]
  [when-esm-modules-ready+ [esm-codemirror* esm-codemirror-view* esm-lang-mermaid*]
   [codemirror-mermaid+ text input*]])
