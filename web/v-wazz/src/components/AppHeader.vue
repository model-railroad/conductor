<script setup lang="ts">
import {BCollapse, BLink, BNav, BNavbar, BNavbarBrand, BNavbarToggle, BNavItem, BNavText} from "bootstrap-vue-next";
import {GIT_BRANCH_STR, GIT_HASH_STR, GIT_LONG_STR, VERSION_MAJOR, VERSION_MINOR} from "../GitBuild.ts";
</script>

<template>
  <BNavbar toggleable="md" variant="dark" data-bs-theme="dark" >
    <BNavbarBrand>
      Randall Trains Status
    </BNavbarBrand>
    <div class="flex-grow-1">
      <BNav underline class="nav-font-size flex-row justify-content-start justify-content-md-start">
        <BNavItem :active="true">Dashboard</BNavItem>
        <BNavItem :active="false" disabled>Performance</BNavItem>
      </BNav>
    </div>
    <BNavbarToggle target="text-nav-info" />
    <BCollapse is-nav id="text-nav-info" class="navbar-detail justify-content-end">
      <BNavText>
        <a href="https://www.alfray.com/trains/randall/" target="_blank">The Randall Museum Model Railroad</a>
        <br/>
        <BLink to="/statz">σ</BLink>
        v{{VERSION_MAJOR}}.{{VERSION_MINOR}},
        <span class="gitinfo" :title="GIT_LONG_STR">
            <a :href="`https://github.com/model-railroad/conductor/tree/${GIT_BRANCH_STR}`" target="_blank">
                build {{GIT_BRANCH_STR}}
            </a>
            @
            <a :href="`https://github.com/model-railroad/conductor/commit/${GIT_HASH_STR}`" target="_blank">
              {{GIT_HASH_STR}}
            </a>
        </span>
      </BNavText>
    </BCollapse>
  </BNavbar>
</template>
