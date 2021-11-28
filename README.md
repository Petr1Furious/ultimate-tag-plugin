# Ultimate tag plugin

A Minecraft plugin inspired by an "Ultimate tag" Minecraft minigame.

At the start of each round some players are automatically selected as runners, while the rest of them are hunters. They spawn in a 150x150 world. The runners have to survive for a given time, while hunters have to hit each runner at least once. If a hunter hits a runner, the runner instantly dies. All the runners get randon tools, while all the hunters get compasses, which are pointing at the closest runner.

Any number of players is supported.

## Installation

Requires Spigot/Paper server for Minecraft 1.17.1.

Put built plugin file into the plugins folder and start the server.

## Usage

To access the help menu, execute "/tag".

To start the game, first execute "/tag setup [hunters count]".

To join all players in the game, execute "/tag join \*".

To start the game, execute "/tag start". After that the plugin will automatically run all the rounds the way that every set of exactly "hunters count" players will be the hunters team once.

After the game finishes, the host can either finish it by running "/tag end" or start another series of rounds with the same players by running "/tag start".

## Example
