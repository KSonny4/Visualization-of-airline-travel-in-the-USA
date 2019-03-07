import networkx as nx
G = nx.read_graphml("airlines.graphml")
for node in G._node:
    print(G._node[node]['tooltip']) #lip jsem to zatim nevymyslel
