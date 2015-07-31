/**
 * Copyright (C) 2013 Red Hat, Inc. (jdcasey@commonjava.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.maven.cartographer.testutil;

import java.util.concurrent.Executors;

import org.commonjava.maven.atlas.graph.RelationshipGraph;
import org.commonjava.maven.atlas.graph.ViewParams;
import org.commonjava.maven.atlas.graph.spi.RelationshipGraphConnectionFactory;
import org.commonjava.maven.atlas.graph.spi.neo4j.FileNeo4jConnectionFactory;
import org.commonjava.maven.cartographer.Cartographer;
import org.commonjava.maven.cartographer.CartographerBuilder;
import org.commonjava.maven.cartographer.agg.DefaultGraphAggregator;
import org.commonjava.maven.cartographer.agg.GraphAggregator;
import org.commonjava.maven.cartographer.data.CartoDataException;
import org.commonjava.maven.cartographer.discover.ProjectRelationshipDiscoverer;
import org.commonjava.maven.cartographer.ops.ResolveOps;
import org.commonjava.maven.cartographer.util.MavenModelProcessor;
import org.commonjava.maven.galley.maven.GalleyMaven;
import org.commonjava.maven.galley.testing.maven.GalleyMavenFixture;

public class CartoFixture
    extends GalleyMavenFixture
{

    private CartographerBuilder builder;

    private Cartographer cartographer;

    private boolean graphAggregatorSet;

    public CartoFixture()
    {
        super();
    }

    protected void initCartographer()
        throws Exception
    {
        if ( builder == null )
        {
            super.initMissingComponents();

            final RelationshipGraphConnectionFactory connectionFactory =
                new FileNeo4jConnectionFactory( getTemp().newFolder( "graph.", ".db" ), false );

            final GalleyMaven galleyMaven = getGalleyMaven();
            System.out.println( "Initialized galley-maven components: " + galleyMaven );
            builder = new CartographerBuilder( galleyMaven, connectionFactory );

            builder.withDiscoverer( new TestAggregatorDiscoverer() );
            if ( !graphAggregatorSet )
            {
                builder.withGraphAggregator( newGraphAggregator( builder.getDiscoverer() ) );
            }
        }
    }

    private GraphAggregator newGraphAggregator( final ProjectRelationshipDiscoverer discoverer )
    {
        return new DefaultGraphAggregator( discoverer, Executors.newFixedThreadPool( 1 ) );
    }

    @Override
    public void initMissingComponents()
        throws Exception
    {
        super.initMissingComponents();
        initCartographer();
    }

    public TestAggregatorDiscoverer getDiscoverer()
    {
        return (TestAggregatorDiscoverer) builder.getDiscoverer();
    }

    public GraphAggregator getAggregator()
    {
        return builder.getGraphAggregator();
    }

    public ResolveOps getResolveOps()
    {
        return cartographer == null ? null : cartographer.getResolver();
    }

    public void setDiscoverer( final TestAggregatorDiscoverer discoverer )
        throws Exception
    {
        if ( !graphAggregatorSet )
        {
            builder.withDiscoverer( discoverer );
            setAggregator( getAggregator() );
        }
        else
        {
            throw new IllegalStateException( "Aggregator already initialized!" );
        }
    }

    public void setAggregator( final GraphAggregator aggregator )
        throws Exception
    {
        builder.withGraphAggregator( aggregator );
        graphAggregatorSet = true;
        cartographer();
    }

    @Override
    public void before()
        throws Exception
    {
        cartographer();
    }

    @Override
    public void after()
    {
        try
        {
            if ( cartographer != null )
            {
                cartographer.close();
                cartographer = null;
            }
        }
        catch ( final CartoDataException e )
        {
            e.printStackTrace();
        }

        super.after();
    }

    public Cartographer cartographer()
        throws Exception
    {
        if ( cartographer == null )
        {
            initMissingComponents();
            initCartographer();
            cartographer = builder.build();
        }

        return cartographer;
    }

    public RelationshipGraph openGraph( final ViewParams params, final boolean create )
        throws Exception
    {
        return cartographer().getGraphFactory()
                             .open( params, create );
    }

    public MavenModelProcessor getModelProcessor()
    {
        return new MavenModelProcessor();
    }

}