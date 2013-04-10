/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2012 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.batch.issue;

import com.google.common.base.Objects;
import org.sonar.api.BatchComponent;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.core.issue.DefaultIssue;
import org.sonar.core.issue.OnIssueCreation;

import java.util.Collection;
import java.util.Date;

public class ModuleIssues implements OnIssueCreation, BatchComponent {

  private final RulesProfile qProfile;
  private final IssueCache cache;

  public ModuleIssues(RulesProfile qProfile, IssueCache cache) {
    this.qProfile = qProfile;
    this.cache = cache;
  }

  public Collection<Issue> issues(String componentKey) {
    // TODO copy
    return cache.componentIssues(componentKey);
  }

  @Override
  public void onIssueCreation(DefaultIssue issue) {
    ActiveRule activeRule = qProfile.getActiveRule(issue.ruleRepositoryKey(), issue.ruleKey());
    if (activeRule == null || activeRule.getRule() == null) {
      // rule does not exist or is not enabled -> ignore the issue
      return;
    }

    // TODO date of scan
    issue.setCreatedAt(new Date());

    if (issue.severity() == null) {
      issue.setSeverity(Objects.firstNonNull(activeRule.getSeverity().name(), Issue.SEVERITY_MAJOR));
    }

    cache.add(issue);
  }
}
