# Mavenizer [![Build Status](https://travis-ci.org/ivy-supplements/mavenizer.svg?branch=master)](https://travis-ci.org/ivy-supplements/mavenizer)
Work effective with maven natured IvyProjects

## Smootly integrate IAR dependencies via Maven
Often your Axon.ivy application is built with many fine separated Axon.ivy projects that depend on each other. But most of the time you only edit on one or two of it. But the Designer forces you to import all dependencies manually into your workspace. This wastes time and triggers many build and validation cycles on projects that you are actually not editing.

And here comes this cool extension into to game. Once activated it will automatically add dependencies of your project directly into your workspace. These dependencies are resolved via Maven and imported as IAR. This make your workspace builds fast and effective.

## Usage
1. Install the Plugin
1. Convert your IvyProject into a Maven natured project: Java Perspective > right click on project > configure > convert to Maven Project
1. Add IAR dependencies to your project that are available via Maven 

## PreCondition
1. The IAR dependencies you require are accessible in a Maven repository

## Installation
1. Open a Designer (6.0.0 or new) > Help > Install New Software
1. Copy the URI https://ivy-supplements.github.io/mavenizer/ into the "Work with" text field on top
1. Presse next, accept the license and agree to install unsigned content
1. Restart the Designer as suggested when the installation process ends
