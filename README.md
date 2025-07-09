# My homepage on the world wide web

## Requirements

* [babashka](https://github.com/babashka/babashka)
* OpenSSL

## Local previews

* `bb src/render.clj; open target/index.html`

## Deployment

* Deployed on [Netlify](https://www.netlify.com/)
* Two env vars need to be set
  * `NETLIFY_TOKEN` - [can be created here](https://app.netlify.com/user/applications)
  * `NETLIFY_SITE_ID` - can be found in project settings (also called "project id")
* Deployed with a babashka script: `bb src/deploy.clj`

___

2012 - &infin; [skazhy](https://karlis.me)
