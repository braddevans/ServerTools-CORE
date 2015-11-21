/*
 * This file is a part of ServerTools <http://servertools.info>
 *
 * Copyright (c) 2014 ServerTools
 * Copyright (c) 2014 contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.servertools.core.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec3i;

import javax.annotation.Nullable;
import java.util.Objects;

public class Location {

    private final int dim;
    private final double x;
    private final double y;
    private final double z;

    public Location(final int dim, final double x, final double y, final double z) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location(final int dim, final Vec3 vec3) {
        Objects.requireNonNull(vec3, "vec3");
        this.dim = dim;
        this.x = vec3.xCoord;
        this.y = vec3.yCoord;
        this.z = vec3.zCoord;
    }

    public Location(final int dim, final Vec3i vec3i) {
        Objects.requireNonNull(vec3i, "vec3i");
        this.dim = dim;
        this.x = vec3i.getX();
        this.y = vec3i.getY();
        this.z = vec3i.getZ();
    }

    public Location(final Entity entity) {
        Objects.requireNonNull(entity, "entity");
        this.dim = entity.worldObj.provider.getDimensionId();
        this.x = entity.posX;
        this.y = entity.posY;
        this.z = entity.posZ;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int getDim() {
        return dim;
    }

    @Override
    public String toString() {
        return "Location{" + "x=" + x + ", y=" + y + ", z=" + z + ", dim=" + dim + '}';
    }

    @Override
    public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Location location = (Location) o;
        return Double.compare(location.x, x) == 0 &&
                Double.compare(location.y, y) == 0 &&
                Double.compare(location.z, z) == 0 &&
                dim == location.dim;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, dim);
    }
}
