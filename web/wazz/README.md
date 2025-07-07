# Wazz: Randall Trains Status

[Wazz](https://ralf.alfray.com/trains/randall/wazz/)
is a React-TypeScript single-page website
that displays the status of the trains at the
[Randall Museum Model Railroad](https://www.alfray.com/trains/randall/).

The Randall Museum in San Francisco hosts a large HO-scale model train layout.
The layout is fully operating in DCC with some parts of the track featuring
train running automatically under the supervision of the
[Randall Train Automation Controller](http://ralf.alfray.com/trains/randall_rtac.html).

The [Conductor](http://ralf.alfray.com/trains/randall_rtac.html) automation software
exports its status via JSON, which [Wazz](https://ralf.alfray.com/trains/randall/wazz/)
displays.


## Building

Wazz uses:
- [Node JS](https://nodejs.org/): 24.0.0,
- [React with TypeScript](https://react.dev/learn/typescript),
- [Vite](https://vite.dev/) for build and deployment.

To build and run the dev server:
```(shell)
# setup node.js environment, e.g. something like:
$ nvm use
$ fnm use

# build and run:
$ npm install
$ npm run dev
```

To build the production site:
```(shell)
$ npm install
$ npm run build
```


## License

Wazz is licensed under the
[GNU GPL v3 license](https://opensource.org/license/gpl-3-0).
See the [LICENSE.txt](./LICENSE.txt) file for details.

~~
