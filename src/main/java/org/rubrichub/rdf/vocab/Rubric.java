package org.rubrichub.rdf.vocab;

import com.hp.hpl.jena.rdf.model.*;

public class Rubric
{
	protected static final String uri ="http://rubrichub.org/owl/2010/01/rubric.owl#";
	
	private static Model m = ModelFactory.createDefaultModel();
	
	public static final Resource Rubric = m.createResource(uri + "Rubric" );
	public static final Resource AnalyticRubric = m.createResource(uri + "AnalyticRubric" );
	public static final Resource HolisticRubric = m.createResource(uri + "HolisticRubric" );
	public static final Resource ChecklistRubric = m.createResource(uri + "ChecklistRubric" );
	public static final Resource Scope = m.createResource(uri + "Scope" );
	public static final Resource Category = m.createResource(uri + "Category" );
	public static final Resource CriteriaList = m.createResource(uri + "CriteriaList" );
	public static final Resource Criterion = m.createResource(uri + "Criterion" );
	public static final Resource LevelList = m.createResource(uri + "LevelList" );
	public static final Resource Level = m.createResource(uri + "Level" );
	public static final Resource scopeSelf = m.createResource(uri + "self" );
	public static final Resource scopeIndividual = m.createResource(uri + "individual" );
	public static final Resource scopeTeam = m.createResource(uri + "team" );
	public static final Resource scopePeer = m.createResource(uri + "peer" );

	public static final Property hasScope = m.createProperty(uri, "hasScope" );
	
	public static final Property hasCriteria = m.createProperty(uri, "hasCriteria" );
	public static final Property hasElements = m.createProperty(uri, "hasElements" );
	public static final Property hasLevels = m.createProperty(uri, "hasLevels" );
	
	public static final Property title = m.createProperty(uri, "title" );
	public static final Property description = m.createProperty(uri + "description" );

	public static final Property score = m.createProperty(uri, "score" );
	public static final Property weight = m.createProperty(uri, "weight" );
	public static final Property benchmark = m.createProperty(uri, "benchmark" );
	public static final Property feedback = m.createProperty(uri, "feedback" );

	public static String getURI() {
		return uri;
	}
}
