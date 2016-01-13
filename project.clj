(defproject pig-squeal "0.1.0"
  :description "Pig-Squeal: A Clojure library for pgSQL (PostgreSQL)"
  :url "http://github.com/cloojure/pig-squeal"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [ [org.clojure/clojure              "1.7.0"]
                  [org.clojure/core.incubator       "0.1.3"]
                  [org.clojure/test.check           "0.5.9"]
                  [org.clojure/core.match           "0.3.0-alpha4"]
                  [org.clojure/java.jdbc            "0.4.2"] 
                  [java-jdbc/dsl                    "0.1.3"]
                  [org.postgresql/postgresql        "9.4-1206-jdbc42"]
                  [com.mchange/c3p0                 "0.9.5.2"]
                  [clojure-csv/clojure-csv          "2.0.1"]
                  [clj-time                         "0.7.0"]
                  [criterium                        "0.4.3"]
                  [cheshire                         "5.5.0"]
                  [prismatic/schema                 "1.0.4"]
                ]
  :plugins  [ [codox "0.8.10"] ]
  :codox {:src-dir-uri "http://github.com/cloojure/pig-squeal/blob/master/"
          :src-linenum-anchor-prefix "L"}
  :deploy-repositories {  "snapshots" :clojars
                          "releases"  :clojars }
  :update :daily  ; :always
  :main ^:skip-aot pig-squeal.core
  :target-path "target/%s"
  :clean-targets [ "target" ]
  :profiles { ; :dev      { :certificates ["clojars.pom"] }
              :uberjar  { :aot :all }
            }

  ; "lein test"         will not  run tests marked with the ":slow" metadata
  ; "lein test :slow"   will only run tests marked with the ":slow" metadata
  ; "lein test :all"    will run all  tests (built-in)
  :test-selectors { :default    (complement :slow)
                    :slow       :slow }

  :jvm-opts ["-Xms1g" "-Xmx4g" ]
; :jvm-opts ["-Xms4g" "-Xmx8g" "-server"]
)
