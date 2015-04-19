(ns lambdacd-cctray.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clj-cctray.core :as parser]
            [clj-time.core :as t]
            [lambdacd.steps.control-flow :as control-flow]
            [lambdacd-cctray.core :refer :all]))

(defn some-name [& _])
(defn some-other-name [& _])

(def pipeline-def
  `(
     some-name
     (control-flow/either
       some-other-name)))

(def some-state {8 { [1] {:status :success
                          :most-recent-update-at (t/date-time 2015 1 2 3 40 0)}}
                 3 { [1 2] {:status :running
                            :most-recent-update-at (t/date-time 2015 1 2 3 40 1)}}
                 2 { [1 2] {:status :failure
                            :most-recent-update-at (t/date-time 2015 1 2 3 40 2)}}})

(deftest cc-xmltray-for-test
  (testing "That it produces a valid cctray-xml"
    (let [xmlstring (cctray-xml-for pipeline-def some-state  "some/base/url")
          xmlstream (io/input-stream (.getBytes xmlstring))
          projects (parser/get-projects xmlstream)]
      (is (= {:name "some-name"
              :activity :sleeping
              :last-build-status :success
              :last-build-label "8"
              :last-build-time (t/date-time 2015 1 2 3 40 0)
              :web-url "some/base/url/#/builds/8/1"
              :messages          []
              :next-build-time   nil
              :prognosis         :healthy} (first projects)))
      (is (= {:name "some-other-name"
               :activity :building
               :last-build-status :failure
               :last-build-label "3"
               :last-build-time (t/date-time 2015 1 2 3 40 1)
               :web-url "some/base/url/#/builds/3/1-2"
               :messages          []
               :next-build-time   nil
               :prognosis         :sick-building} (nth projects 2))))))
