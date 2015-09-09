package org.commonjava.maven.cartographer.util;

import java.util.Map;

import org.commonjava.maven.atlas.graph.mutate.GraphMutator;
import org.commonjava.maven.atlas.graph.mutate.GraphMutatorSelector;

public class CartoGraphMutatorSelector
{

    public static GraphMutator selectMutator( final Map<String, Object> presetParams )
    {
        Object mutatorCode = null;
        if ( presetParams != null )
        {
            mutatorCode = presetParams.get( "mutator" );
        }
        return GraphMutatorSelector.selectMutator( mutatorCode );
    }

}
