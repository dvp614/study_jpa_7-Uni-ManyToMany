package org.zerock.myapp.association;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;
import org.zerock.myapp.entity.Product1;
import org.zerock.myapp.entity.Shopper1;
import org.zerock.myapp.util.PersistenceUnits;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class M2MUniDireactionalMappingTests {
	private EntityManagerFactory emf;
	private EntityManager em;
	
	@BeforeAll
	void beforeAll() {
		log.trace("beforeAll() invoked.");
		
		this.emf = Persistence.createEntityManagerFactory(PersistenceUnits.H2);
		
		this.em = this.emf.createEntityManager();
		assertNotNull(this.em);
	} // beforeAll
	
	@AfterAll
	void afterAll() {
		log.trace("afterAll() invoked.");
	
		this.em.clear();
		try { this.em.close(); } catch (Exception _ignored) {}
		try { this.emf.close(); } catch (Exception _ignored) {}
	} // afterAll
	
//	@Disabled
	@Order(1)
	@Test
//	@RepeatedTest(1)
	@DisplayName("1. prepareData")
	@Timeout(value = 5L, unit = TimeUnit.MINUTES)
	void prepareData() {
		log.trace("prepareData invoked.");
		
		try {
			this.em.getTransaction().begin();
			
			IntStream.rangeClosed(1, 5).forEachOrdered(seq -> {
				Shopper1 transientShopper = new Shopper1();
				
				transientShopper.setName("Shopper_" + seq);

				this.em.persist(transientShopper);
			}); // .forEachOrdered
			
			Shopper1 s1 = this.em.<Shopper1>find(Shopper1.class, 1L);
			Shopper1 s2 = this.em.<Shopper1>find(Shopper1.class, 2L);
			Shopper1 s3 = this.em.<Shopper1>find(Shopper1.class, 3L);
			Shopper1 s4 = this.em.<Shopper1>find(Shopper1.class, 4L);
			Shopper1 s5 = this.em.<Shopper1>find(Shopper1.class, 5L);
			
			Objects.requireNonNull(s1);
			Objects.requireNonNull(s2);
			Objects.requireNonNull(s3);
			Objects.requireNonNull(s4);
			Objects.requireNonNull(s5);
			
			IntStream.rangeClosed(1, 20).forEach(seq -> {
				Product1 transientShopper= new Product1();
				transientShopper.setName("Product_" + seq);
				
				if(seq >= 15) 		s1.getProducts().add(transientShopper);
				else if(seq >= 10) 	s2.getProducts().add(transientShopper);
				else if(seq >= 8)	s3.getProducts().add(transientShopper);
				else if(seq >= 3)	s4.getProducts().add(transientShopper);
				else 				s5.getProducts().add(transientShopper);
				
				this.em.persist(transientShopper);
			});
			
			
			
			this.em.getTransaction().commit();
		}catch(Exception e) {
			this.em.getTransaction().rollback();
			throw e;
		} // try-catch
		
		
		
	} // prepareData
	
//	@Disabled
	@Order(2)
	@Test
//	@RepeatedTest(1)
	@DisplayName("2. testM2MUniObjectGraphTraverseFromShopperToProduct")
	@Timeout(value = 5L, unit = TimeUnit.MINUTES)
	void testM2MUniObjectGraphTraverseFromShopperToProduct() {
		log.trace("testM2MUniObjectGraphTraverseFromShopperToProduct invoked.");
		
		var shopperId = new Random().nextLong(1L, 3L);
		Shopper1 shopper = this.em.<Shopper1>find(Shopper1.class, shopperId);
		log.info("\t+ shopper : {}", shopper);
		
		Objects.requireNonNull(shopper);
		
		shopper.getProducts().forEach(p -> log.info(p.toString()));
	} // testM2MUniObjectGraphTraverseFromShopperToProduct
	
	
} // end class
