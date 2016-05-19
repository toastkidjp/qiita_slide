Qiita Slide Show Generator
====

This application can generate HTML slide.

# Usage

## simple generating

```sh
$ java -jar qiita_slide.jar http://qiita.com/y_q1m/items/8edef17bf0d86abff226
Generate slide html to slide.html
```

## with stylesheet
```sh
$ java -jar qiita_slide.jar http://qiita.com/y_q1m/items/8edef17bf0d86abff226 sky
Generate slide html to slide.html
```

## parameters
You should pass two arguments.

args[0]……Qiita Article's URL
args[1]……Theme name

You can use theme 
[serif, moon, sky, simple, solarized, beige, white, night, blood, league, black]

# LICENSE
This application licensed Eclipse Public License version 1.0.

# Special thanks
This application powered by [reveal.js](https://github.com/hakimel/reveal.js/).
