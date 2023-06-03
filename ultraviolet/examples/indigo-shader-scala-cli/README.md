# Ultraviolet + Indigo + Scala-CLI

This is a little example of using Scala-CLI to package up a standalone Indigo shader written in Ultraviolet, and run it as an electron app.

# Building

In the same directory as this README, run the following command.

```sh
scala-cli --power package -f -o ./app/scripts/main.js SimpleVoronoi.scala
```

This compiles the code and outputs an uncompressed (it'll be big...) `main.js` file in the `./app/scripts/` directory.

# Running

Assumes you have `yarn` installed, `npm` should be the same.

```sh
cd app
yarn install
yarn start
```
