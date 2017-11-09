package de.mannheim.uni.matching.correspondences;

import de.dwslab.T2K.matching.correspondences.Correspondence;
import de.dwslab.T2K.matching.correspondences.CorrespondenceGenerator;
import java.util.Collection;
import java.util.Iterator;

import de.mannheim.uni.matching.TestInstance;
import de.dwslab.T2K.similarity.matrix.ArrayBasedSimilarityMatrixFactory;
import de.dwslab.T2K.similarity.matrix.SimilarityMatrix;
import junit.framework.TestCase;

public class CorrespondenceGeneratorTest extends TestCase {
	
	public void testGenerateCorrespondences() {
		SimilarityMatrix<TestInstance> sim = new ArrayBasedSimilarityMatrixFactory().createSimilarityMatrix(2, 2);
		
		TestInstance left1 = new TestInstance(1);
		TestInstance left2 = new TestInstance(2);
		TestInstance right1 = new TestInstance(11);
		TestInstance right2 = new TestInstance(22);
		
		sim.set(left1, right1, 1.0);
		sim.set(left2, right2, 1.0);
		
		CorrespondenceGenerator generator = new CorrespondenceGenerator();
		
		Collection<Correspondence<TestInstance>> correspondences = generator.generateCorrespondences(sim, 0.0);
		assertEquals(2, correspondences.size());
		
		Iterator<Correspondence<TestInstance>> iterator = correspondences.iterator();
		
		Correspondence<TestInstance> cor = iterator.next();
		Correspondence<TestInstance> first, second;
		
		if(cor.getFirst().getId()==1) {
		    first = cor;
		    second = iterator.next();
		} else {
		    second = cor;
		    first = iterator.next();
		}
		
		
		System.out.println(String.format("%s <> %s (%.2f)", first.getFirst().getId(), first.getSecond().getId(), first.getSimilarity()));
		assertSame(left1, first.getFirst());
		assertSame(right1, first.getSecond());
		assertEquals(1.0, first.getSimilarity());
		System.out.println(String.format("%s <> %s (%.2f)", second.getFirst().getId(), second.getSecond().getId(), second.getSimilarity()));
		assertSame(left2, second.getFirst());
		assertSame(right2, second.getSecond());
		assertEquals(1.0, second.getSimilarity());
	}

}
