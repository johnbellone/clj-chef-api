# Change Log
All notable changes to this project will be documented in this file.

## [Unreleased]
### Added
- Added easier to use functions for `spoon.search`, which take care of setting
  the query and pagination parameters.

### Deprecated
- The old `get-*` functions in `spoon.search` should be avoided in favor of the
  new higher level search functions.

### Removed
- User search was removed. This doesn't seem to be an index that is available
  according to the GET indexes call.

[Unreleased]: https://github.com/johnbellone/spoon/compare/v0.3.1...HEAD 
