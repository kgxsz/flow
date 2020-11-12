(ns flow.styles.constants)


(def colour
  {:black-one "#000000"
   :black-two "#333333"
   :black-three "#454545"
   :black-four "#666666"
   :grey-one "#AAAAAA"
   :grey-two "#BBBBBB"
   :grey-three "#D8D8D8"
   :grey-four "#EAEAEA"
   :white-one "#FFFFFF"
   :white-two "#FDFDFD"
   :white-three "#F2F2F2"
   :green-one "#FBFDF8"
   :green-two "#8ACA55"
   :blue-two "#58A1F5"
   :orange-two "#EE8F66"
   :purple-two "#D4A3E3"
   :yellow-one "#FEFCF8"
   :yellow-two "#FADA6E"
   :red-one "#FFFAFB"
   :red-two "#EB5468"})

(def spacing
  {:xxx-tiny 1
   :xx-tiny 2
   :x-tiny 3
   :tiny 4
   :xxx-small 6
   :xx-small 8
   :x-small 10
   :small 12
   :medium 16
   :large 20
   :x-large 30
   :xx-large 40
   :xxx-large 50
   :huge 60
   :x-huge 100
   :xx-huge 200
   :xxx-huge 360})

(def filling
  {:xxx-tiny 1
   :xx-tiny 2
   :x-tiny 3
   :tiny 4
   :xxx-small 6
   :xx-small 10
   :x-small 14
   :small 18
   :medium 24
   :large 30
   :x-large 40
   :xx-large 50
   :xxx-large 70
   :huge 100
   :x-huge 140
   :xx-huge 210
   :xxx-huge 270})

(def breakpoint
  {:tiny {:end 319}
   :small {:start 320 :end 479}
   :medium {:start 480 :end 767}
   :large {:start 768 :end 1023}
   :huge {:start 1024}})

(def radius
  {:tiny 1
   :small 2
   :medium 3
   :large 4
   :huge 8
   :x-huge 20})

(def proportion
  {:0 0
   :5 5
   :10 10
   :15 15
   :20 20
   :25 25
   :30 30
   :35 35
   :40 40
   :45 45
   :50 50
   :55 55
   :60 60
   :65 65
   :70 70
   :75 75
   :80 80
   :85 85
   :90 90
   :95 95
   :100 100})


(def fraction
  {:0 0
   :5 0.05
   :10 0.1
   :15 0.15
   :20 0.2
   :25 0.25
   :30 0.3
   :35 0.35
   :40 0.4
   :45 0.45
   :50 0.5
   :55 0.55
   :60 0.6
   :65 0.65
   :70 0.7
   :75 0.75
   :80 0.8
   :85 0.85
   :90 0.9
   :95 0.95
   :100 0.1})


(def font-size
  {:xx-tiny 7
   :x-tiny 8
   :tiny 9
   :xxx-small 10
   :xx-small 11
   :x-small 12
   :small 13
   :medium 14
   :large 15
   :x-large 16
   :xx-large 17
   :xxx-large 18
   :huge 20
   :x-huge 24
   :xx-huge 32
   :xxx-huge 40})
