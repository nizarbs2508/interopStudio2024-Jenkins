package com.ans.cda.fhir;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CodeSystem
 * 
 * @author nbensalem
 */
public class CodeSystem {
	/**
	 * CONCEPTPROPERTY_PREFIX
	 */
	public static final String CONCEPTPROPERTY_PREFIX = "http://hl7.org/fhir/concept-properties";
	/**
	 * CONCEPTPROPERTY_STATUS
	 */
	public static final String CONCEPTPROPERTY_STATUS = CONCEPTPROPERTY_PREFIX + "#status";
	/**
	 * CONCEPTPROPERTY_RETIREMENT_DATE
	 */
	public static final String CONCEPTPROPERTY_RETIREMENT_DATE = CONCEPTPROPERTY_PREFIX + "#retirementDate";
	/**
	 * CONCEPTPROPERTY_DEPRECATION_DATE
	 */
	public static final String CONCEPTPROPERTY_DEPRECATION_DATE = CONCEPTPROPERTY_PREFIX + "#deprecationDate";
	/**
	 * CONCEPTPROPERTY_PARENT
	 */
	public static final String CONCEPTPROPERTY_PARENT = CONCEPTPROPERTY_PREFIX + "#parent";
	/**
	 * CONCEPTPROPERTY_NOT_SELECTABLE
	 */
	public static final String CONCEPTPROPERTY_NOT_SELECTABLE = CONCEPTPROPERTY_PREFIX + "#notSelectable"; // boolean
	/**
	 * CONCEPTPROPERTY_INACTIVE
	 */
	public static final String CONCEPTPROPERTY_INACTIVE = CONCEPTPROPERTY_PREFIX + "#inactive"; // boolean
	/**
	 * CONCEPTPROPERTY_DEPRECATED
	 */
	public static final String CONCEPTPROPERTY_DEPRECATED = CONCEPTPROPERTY_PREFIX + "#deprecated"; // datetime
	/**
	 * properties
	 */
	private List<Property> properties = new ArrayList<>();

	/**
	 * getProperties
	 * @return
	 */
	public List<Property> getProperties() {
		return properties;
	}

	/**
	 * Property
	 */
	public static class Property {
		/**
		 * uri
		 */
		private String uri;
		/**
		 * code
		 */
		private String code;

		/**
		 * Property
		 * @param uri
		 * @param code
		 */
		public Property(String uri, String code) {
			this.uri = uri;
			this.code = code;
		}

		/**
		 * getUri
		 * @return
		 */
		public String getUri() {
			return uri;
		}

		/**
		 * getCode
		 * @return
		 */
		public String getCode() {
			return code;
		}
	}

	/**
	 * ConceptDefinitionComponent
	 */
	public static class ConceptDefinitionComponent {
		private List<ConceptPropertyComponent> properties = new ArrayList<>();

		public List<ConceptPropertyComponent> getProperties() {
			return properties;
		}
	}

	/**
	 * ConceptPropertyComponent
	 */
	public static class ConceptPropertyComponent {
		/**
		 * code
		 */
		private String code;
		/**
		 * ConceptPropertyComponent
		 * @param code
		 */
		public ConceptPropertyComponent(String code) {
			this.code = code;
		}

		/**
		 * getCode
		 * @return
		 */
		public String getCode() {
			return code;
		}
	}

	/**
	 * CodeSystemExtensions
	 */
	public static class CodeSystemExtensions {
		/**
		 * listConceptProperties
		 * 
		 * @param concept
		 * @param system
		 * @param uri
		 * @return
		 */
		public static List<ConceptPropertyComponent> listConceptProperties(ConceptDefinitionComponent concept,
				CodeSystem system, String uri) {
			// Map the URI to a code in the CodeSystem
			String code = system.getProperties().stream().filter(p -> p.getUri().equals(uri)).map(Property::getCode)
					.findFirst().orElse(null);

			// Query the concept's properties
			if (code != null) {
				return concept.getProperties().stream().filter(p -> p.getCode().equals(code))
						.collect(Collectors.toList());
			}

			return new ArrayList<>();
		}
	}
}
