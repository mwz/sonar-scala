/*
 * Sonar Scoverage Plugin
 * Copyright (C) 2013 Rado Buransky
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.buransky.plugins.scoverage.pathcleaner

import com.buransky.plugins.scoverage.util.PathUtil.PathSeq

/**
 * @author Michael Zinsmaier
 */
trait PathSanitizer {

  /** tries to convert the given path such that it is relative to the
   *  projects/modules source directory.
   *
   *  @return Some(source folder relative path) or None if the path cannot be converted
   */
  def getSourceRelativePath(path: PathSeq): Option[PathSeq]
}
