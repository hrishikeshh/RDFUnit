package org.aksw.rdfunit.model.shacl;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import org.aksw.rdfunit.model.helper.PropertyValuePairSet;
import org.aksw.rdfunit.model.interfaces.shacl.ComponentParameter;
import org.aksw.rdfunit.model.interfaces.shacl.PropertyConstraint;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Description
 *
 * @author Dimitris Kontokostas
 * @since 11/2/2016 9:28 πμ
 */
@Builder
public class TemplateRegistry {
    @Getter
    @NonNull
    @Singular
    private final Set<ShaclPropertyConstraintTemplate> shaclCoreTemplates;


    public Set<PropertyConstraint> generatePropertyConstraints(PropertyValuePairSet propertyValuePairSet) {

        return shaclCoreTemplates.stream()
                //get all patterns that can bind to the input
                .filter(p -> p.getComponentParameters().stream().allMatch(a -> a.canBind(propertyValuePairSet)))
                // check if any filter applies
                .filter(p -> p.canBind(propertyValuePairSet))
                // create bindings for each and map to ShaclPropertyConstraintInstance
                .map(p -> {
                    ShaclPropertyConstraintInstance.ShaclPropertyConstraintInstanceBuilder builder = ShaclPropertyConstraintInstance.builder();

                    p.getComponentParameters().stream().forEach(arg -> builder.binding(arg, arg.getBindFromValues(propertyValuePairSet).get()));

                    return builder.template(p).build();

                }).collect(Collectors.toSet());

    }

    public static TemplateRegistry createCore() {
        TemplateRegistryBuilder builder = TemplateRegistry.builder();



        builder.shaclCoreTemplate( createTemplate( CoreArguments.in,
                "$PATH have value: $in",
        "FILTER NOT EXISTS { VALUES ?value { $in }  } } " ));


        builder.shaclCoreTemplate( createTemplateWithFilter( CoreArguments.minCount,
                "Minimum cardinality for $PATH is '$minCount'",
                " } GROUP BY ?this\n" +
                " HAVING ( ( count(?value)  < $minCount ) && ( count(?value)  != 0 ) ) ",
                " ASK { FILTER ($minCount > 1)}"));
        builder.shaclCoreTemplate( createTemplateWithFilterNF( CoreArguments.minCount,
                "Minimum cardinality for $PATH is '$minCount'",
                " FILTER NOT EXISTS { ?this $PATH ?value }} ",
                " FILTER NOT EXISTS { ?this $PATH ?value }} ", // is inverse property like this?
                " ASK { FILTER ($minCount > 0)}"));

        builder.shaclCoreTemplate( createTemplateWithFilter( CoreArguments.maxCount,
                "Maximum cardinality for $PATH is '$maxCount'",
                " } GROUP BY ?this\n" +
                " HAVING ( ( count(?value)  > $maxCount ) && ( count(?value)  != 0 ) ) ",
                " ASK { FILTER ($maxCount > 0)}"));
        builder.shaclCoreTemplate( createTemplateWithFilterNF( CoreArguments.maxCount,
                "Maximum cardinality for $PATH is '$maxCount'0",
                " FILTER EXISTS { ?this $PATH ?value }} ",
                " FILTER EXISTS { ?this $PATH ?value }} ", // is inverse property like this?
                " ASK { FILTER ($maxCount = 0)}"));




        //TODO sh:node

        //TODO sh:qualifiedValueShape, sh:qualifiedMinCount, sh:qualifiedMaxCount

        return builder.build();
    }

    private static ShaclPropertyConstraintTemplate createTemplate(ComponentParameter componentParameter, String message, String sparqlSnippet) {
        return ShaclPropertyConstraintTemplate.builder()
                .componentParameter(componentParameter)
                .componentParameter(CoreArguments.path)
                .componentParameter(CoreArguments.severity)
                .message(message)
                .sparqlPropSnippet(sparqlSnippet)
                .sparqlInvPSnippet(sparqlSnippet)
                .includePropertyFilter(true)
                .build();
    }

    private static ShaclPropertyConstraintTemplate createTemplateWithFilter(ComponentParameter componentParameter, String message, String sparqlSnippet, String filter) {
        return ShaclPropertyConstraintTemplate.builder()
                .componentParameter(componentParameter)
                .componentParameter(CoreArguments.path)
                .componentParameter(CoreArguments.severity)
                .message(message)
                .sparqlPropSnippet(sparqlSnippet)
                .sparqlInvPSnippet(sparqlSnippet)
                .argumentFilter(componentParameter, filter)
                .includePropertyFilter(true)
                .build();
    }

    private static ShaclPropertyConstraintTemplate createTemplateWithFilterNF(ComponentParameter componentParameter, String message, String sparqlPropSnippet, String sparqlInvPSnippet, String filter) {
        return ShaclPropertyConstraintTemplate.builder()
                .componentParameter(componentParameter)
                .componentParameter(CoreArguments.path)
                .componentParameter(CoreArguments.severity)
                .message(message)
                .sparqlPropSnippet(sparqlPropSnippet)
                .sparqlInvPSnippet(sparqlInvPSnippet)
                .argumentFilter(componentParameter, filter)
                .includePropertyFilter(false)
                .build();
    }

    private static ShaclPropertyConstraintTemplate createTemplate(ComponentParameter componentParameter1, ComponentParameter componentParameter2, String message, String sparqlSnippet) {
        return ShaclPropertyConstraintTemplate.builder()
                .componentParameter(componentParameter1)
                .componentParameter(componentParameter2)
                .componentParameter(CoreArguments.path)
                .componentParameter(CoreArguments.severity)
                .message(message)
                .sparqlPropSnippet(sparqlSnippet)
                .sparqlInvPSnippet(sparqlSnippet)
                .includePropertyFilter(true)
                .build();
    }
}
