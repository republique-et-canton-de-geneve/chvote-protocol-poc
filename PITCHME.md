## CHvote

##### PoC of the new Internet Voting protocol

---

## Motivation for the new protocol

* Internet Voting commonly perceived as high risk
* Need to increase trust
  * Vote confidentiality
  * Tally validity
* Also: updated legal bases <!-- .element: class="fragment" -->

---

## Motivation for the PoC

* Validate protocol
* Estimate performance
* Improve internal (State of Geneva) grasp on concepts

---

## Outline

* Context
* Update requirements
* Core protocol concepts
* Implementation overview
* Current results
* Ongoing work

---

## Context - CHvote

* Internet voting system
* 4 referendum / intiatives voting rounds / year
* municipal, cantonal and federal elections
  * usually 2-4 rounds / year
* already in use by several cantons

* developed, hosted and maintained by State of Geneva

---

## Context - Project

* New voting protocol ([BFH](https://e-voting.bfh.ch/))
* PoC implementation (State of Geneva)
* Resulting specification and code have been published
* ETA for usage in production: 2019

---

## Updated requirements > Intro

* New ordinance on Internet Voting (2013, in effect 2014)
    * Technical and administrative requirements
* Compliance levels define allowed electorate percentage
    * 30% / 50% / 100%


See [the website of the Federal Chancellery](https://www.bk.admin.ch/themen/pore/evoting/07979/index.html).

---

## Upd. reqs. > Individual verifiability

> voters must receive proof that the server system has registered the vote as it was entered by the voter on the user 
platform
> – _VEleS, art. 4_

![Individual verifiability codes](doc/assets/indiv-verif-sample.png)

In current version: random codes per voter / response

---

## Questions?

