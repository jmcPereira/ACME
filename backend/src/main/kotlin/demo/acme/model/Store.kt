package demo.acme.model

import org.hibernate.id.SequenceGenerator.SEQUENCE
import javax.persistence.*

@Entity
data class Store(
    @Id @GeneratedValue(strategy=GenerationType.SEQUENCE) var id:Long,
    var code:String,
    @Column(length = 5000) var description:String,
    var name:String, 
    var openingDate: String,
    var storeType: String,
    @OneToMany(cascade = [CascadeType.ALL]) var seasons:MutableList<Season>,
    @OneToOne(cascade = [CascadeType.ALL]) var additionalInfo:CsvEntry?) {
    constructor() : this(0, "", "", "", "", "", mutableListOf(), null)
}

data class StoreAndSeason(var storeId:Long, var season:String?){
    constructor() :this(0, null)
}

@Entity
data class Season(@Id @GeneratedValue(strategy=GenerationType.AUTO)var Long: Int, var season:String?){
    constructor() : this(0,null)
}

@Entity
data class CsvEntry(@Id @GeneratedValue(strategy=GenerationType.AUTO)var id: Int, var specialField1:String?, var specialField2: String?){
    constructor() : this(0, "", "")
}