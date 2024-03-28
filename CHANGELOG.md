# Changelog

## [Unreleased]

### Added

* Automatically prompt to download external documentation for builtin symbols. The documentation language can be
  changed. Documentation supports embedded images as well as references to other symbols.

### Changed

* Improve the "connect to robot" dialog panel. Robots on the same network are automatically detected and listed. If
  selected, the plugin automatically connects to the robot. A manual connection can also be established, if a robot is
  not automatically detected.

### Fixed

* Fix error when trying to rename a symbol.

* Fix error when connecting to a robot.

* Fix bug where optional arguments are not resolved to their respective parameter.

* Fix error when calculating value of string literal with an invalid escape character.
  