(defproject reports "0.1.0-SNAPSHOT"
  :description "HTTP layer to applets functionality"
  :url "https://github.com/akvo/akvo-flow"
  :license {:name "GNU Affero General Public License"
            :url "https://www.gnu.org/licenses/agpl"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [ring "1.1.8"]
                 [clojurewerkz/quartzite "1.0.1"]
                 [org.apache.poi/poi "3.8"]
                 [org.apache.poi/poi-ooxml "3.8"]
                 [exporterapplet "1.0.0"]
                 [org.json/json "20090211"]
                 [jfree/jfreechart "1.0.13"]]
  :warn-on-reflection true)
