# Rename Git Remotes

Tiny Clojure CLI to rename remotes like in the case of changing a github username.


## Usage

```shell
clj -Sdeps "{:deps {jayzawrotny/rename-git-remotes {:mvn/version \"0.1.0\"}}" -m rename-git-remotes.cli my-old-username my-new-username ~/path/to/git/projects
```

## License

Copyright © 2019 Jay Zawrotny

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
