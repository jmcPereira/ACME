package demo.acme

import demo.acme.model.Store
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface StoreRepository : JpaRepository<Store?, Long?>{
    fun findByNameContaining(name: String, pageable: Pageable): Page<Store>
}