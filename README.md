# Collaborative Graphical Editor
Utilizes a client-server set up to create a collaborative graphical editor.

## Overview
This program is composed of a master sketch server, server communicators, editors, and editor communicators that all work together to provide a graphical editor in which multiple users can draw, edit, and remove sketches with one another. Clients execute graphical actions, which are requested to the server. The server then broadcasts the changes made to all editors, which provides a single, unified view of the sketch.
