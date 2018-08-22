# Databus Shared Library
This project is a place to factor out some development effort that has potential to be useful for several databus components.

## Versioning and Publishing Policies
Different databus components might rely on different revisions of this library (i.e. not always the latest revision). Thus, it is important that the version of this library receives an 'up-tick' before `mvn deploy` whenever behaviour of existing provided functionality changes and also especially when there are non-backward-compatible API changes.

**NB:** It is of course okay to keep the version number unchanged during local cycles of `mvn install` for this library and testing the ramifications of changes in depending databus components, that will then pick up the changes under test from you local `~/.m2/repository`.

Please do not publish `-SNAPSHOT` releases of this project.
