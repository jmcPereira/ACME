<template>
  <div class="container">
    <div class="store-table card mt-5">
      <h2 class="card-header text-center">Store Information</h2>
      <div class="store-table-header m-3">
        <p v-if="!dataIsAvailable">Server side data is not ready yet. Should auto refresh soon!</p>
        <div v-else>
          <div class="d-flex justify-content-center">
            <v-pagination
                v-model="page"
                :pages="numberOfPages"
                :range-size="0"
                active-color="#DCEDFF"
                @update:modelValue="updatePageHandler"/>
          </div>
          <p>showing {{ stores.length }} items</p>
          <a href="http://localhost:3000/api/acme.csv">Download information as CSV</a>
        </div>
      </div>
      <table v-if="dataIsAvailable" class="table table-striped" v-cloak>
        <thead class="thead-light text-center">
        <tr>
          <th>Store Id</th>
          <th>Store Code</th>
          <th>Store Description</th>
          <th>
            <div class="name-filter row d-flex justify-content-center">
              <span>Store Name</span>
              <input class="col-6" v-model="nameFilter" placeholder="name filter">
              <div class="col-3">
                <button class="btn btn-sm btn-outline-dark" @click="fetchStoresPage(page)">Apply</button>
              </div>
              <div class="col-2">
                <button class="reset-filter-btn btn btn-close btn-sm" @click="resetFilter"></button>
              </div>
            </div>
          </th>
          <th>Store Opening Date</th>
          <th>Store Type</th>
          <th>Seasons</th>
          <th>Additional Information</th>
        </tr>
        </thead>
        <StoresPage :stores="stores"/>
      </table>
    </div>
  </div>
</template>

<script lang="ts">
import VPagination from "@hennge/vue3-pagination";
import "@hennge/vue3-pagination/dist/vue3-pagination.css";
import {Vue, Options} from 'vue-class-component';
import Store from "@/components/interfaces/Store";
import StoresPage from "@/components/StoresPage.vue";

@Options({
  components: {
    StoresPage,
    VPagination,
  }
})
export default class Stores extends Vue {
  stores = new Array<Store>();
  page = 1;
  numberOfPages = 1;
  nameFilter = "";
  dataIsAvailable = false;

  created() {
    this.isDataAvailable()
        .then((result: any) => {
          if (result) {
            this.fetchStoresPage(this.page);
          } else {
            const interval = setInterval(() => {
                this.isDataAvailable()
                    .then((isAvailable: boolean) => {
                      if (isAvailable) {
                        this.fetchStoresPage(this.page);
                        clearInterval(interval);
                      }
                    });
              }, 5000);
          }
        });
  }

  resetFilter() {
    this.nameFilter = "";
    this.fetchStoresPage(this.page);
  }

  updatePageHandler(page: number) {
    this.fetchStoresPage(page);
  }

  fetchStoresPage(page: number) {
    this.stores = new Array<Store>()
    fetch(`http://localhost:3000/api/getStores?page=${page - 1}&storeNameFilter=${encodeURIComponent(this.nameFilter)}`)
        .then(response => response.json())
        .then(data => {
          this.stores = data.storesPage;
          this.numberOfPages = data.totalPages;
          this.dataIsAvailable = true;
        });
  }

  async isDataAvailable() {
    let result = await (await fetch(`http://localhost:3000/api/isDataAvailable`)).text() == "true";
    this.dataIsAvailable = result;
    return result;
  }
}
</script>

<style>
.Page {
  padding: 15px;
}

.Pagination {
  padding-bottom: 15px;
}
</style>
<style scoped>
[v-cloak] {
  display: none;
}

.store-table-header {
  text-align: center;
}

.store-table {
  min-width: 1115px;
}

.name-filter {
  max-width: 250px;
}

.reset-filter-btn {
  padding: 10px;
}
</style>