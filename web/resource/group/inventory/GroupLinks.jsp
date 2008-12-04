<%@ page language="java" %>
<%@ page errorPage="/common/Error.jsp" %>
<%@ taglib uri="struts-html-el" prefix="html" %>
<%@ taglib uri="struts-tiles" prefix="tiles" %>
<%@ taglib uri="jstl-fmt" prefix="fmt" %>
<%@ taglib uri="jstl-c" prefix="c" %>
<%--
  NOTE: This copyright does *not* cover user programs that use HQ
  program services by normal system calls through the application
  program interfaces provided as part of the Hyperic Plug-in Development
  Kit or the Hyperic Client Development Kit - this is merely considered
  normal use of the program, and does *not* fall under the heading of
  "derived work".
  
  Copyright (C) [2004-2008], Hyperic, Inc.
  This file is part of HQ.
  
  HQ is free software; you can redistribute it and/or modify
  it under the terms version 2 of the GNU General Public License as
  published by the Free Software Foundation. This program is distributed
  in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE. See the GNU General Public License for more
  details.
  
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA.
 --%>


<tiles:importAttribute name="resource" />

<table border="0"><tr><td class="LinkBox">

	<html:link page="/resource/group/Inventory.do?mode=new"><fmt:message key="resource.hub.NewGroupLink"/><html:img page="/images/title_arrow.gif" width="11" height="9" alt="" border="0"/></html:link>
	<br />
    <tiles:insert definition=".resource.common.quickDelete">
      <tiles:put name="resource" beanName="resource"/>
	  <tiles:put name="deleteMessage">
		<fmt:message key="resource.group.inventory.link.DeleteGroup"/>
	  </tiles:put>
    </tiles:insert>
	<br />
    <tiles:insert definition=".resource.common.quickFavorites">
      <tiles:put name="resource" beanName="resource"/>
    </tiles:insert>

</td></tr></table>
