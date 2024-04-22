# Changelog

## [Unreleased]

### Added

* Automatically prompt to download external documentation for builtin symbols. The documentation language can be
  changed. Documentation supports embedded images as well as references to other symbols.

* Add inlay hints which show method usages and parameter name.

* Show parameter info for function calls.

### Changed

* Improve the "connect to robot" dialog panel. Robots on the same network are automatically detected and listed. If
  selected, the plugin automatically connects to the robot. A manual connection can also be established, if a robot is
  not automatically detected.

* Highlight syntax during indexing.

### Fixed

* Fix error when trying to rename a symbol.

* Fix error when connecting to a robot.

* Fix bug where optional arguments are not resolved to their respective parameter.

* Fix error when displaying value of string literal with an invalid escape character.

* Fix error where program wouldn't stop at a breakpoint if it was on the first line.
  