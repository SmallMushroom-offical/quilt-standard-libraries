/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.resource.loader.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.Main;
import net.minecraft.server.WorldStem;

import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

@Mixin(Main.class)
public class MainMixin {
	@Inject(
			method = "main",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/Util;method_43499(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;",
					remap = true
			),
			remap = false
	)
	private static void onStartReloadResources(String[] strings, CallbackInfo ci) {
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(null, null); // First reload
	}

	@ModifyVariable(method = "main", at = @At(value = "STORE"), remap = false)
	private static WorldStem onSuccessfulReloadResources(WorldStem resources) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, resources.resourceManager(), null);
		return resources; // noop
	}

	@ModifyArg(
			method = "main",
			at = @At(
					value = "INVOKE",
					target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V"
			),
			index = 1,
			remap = false
	)
	private static Throwable onFailedReloadResources(Throwable exception) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, null, exception);
		return exception; // noop
	}
}
