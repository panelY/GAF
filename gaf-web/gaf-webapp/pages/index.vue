<template>
  <div>
    <!--<system-admin v-if="isAdmin" />
    <tenant-admin v-else-if="isTenant" />
    <common-user v-else-if="isUser" />
     {{ Math.random() }}-->
    <with-login v-if="isLogin" />
    <no-login v-else />
  </div>
</template>
<script>
import NoLogin from '../views/home/NoLogin'
import WithLogin from '../views/home/WithLogin'
/* import SystemAdmin from '../views/home/SystemAdmin'
import TenantAdmin from '../views/home/TenantAdmin'
import CommonUser from '../views/home/CommonUser' */
export default {
  components: {
    NoLogin,
    WithLogin,
    /* SystemAdmin,
    TenantAdmin,
    CommonUser, */
  },
  computed: {
    isAdmin() {
      return this.$store.getters.isAdmin
    },
    isTenant() {
      return this.$store.getters.isTenant
    },
    isUser() {
      return this.$store.getters.isUser
    },
    isLogin() {
      return this.$store.getters.isLogin
    },
  },
}
</script>
