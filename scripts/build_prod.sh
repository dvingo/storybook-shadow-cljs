#!/bin/bash -

set -eo pipefail

write_prn_file() {
  echo "(ns ex.storybook-shadow-cljs.client.prn-debug)(defn pprint-str [v])(defn pprint [v])" \
    > src/main/ex/storybook_shadow_cljs/client/prn_debug.cljs
}

main() {
  write_prn_file

  echo yarn run client/release
  yarn run client/release
  echo "done"
}

main "$@"
