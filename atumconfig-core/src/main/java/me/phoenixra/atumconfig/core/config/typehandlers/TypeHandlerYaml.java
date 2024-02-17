package me.phoenixra.atumconfig.core.config.typehandlers;

import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigType;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.util.HashMap;
import java.util.Map;

public class TypeHandlerYaml extends ConfigTypeHandler{

    public TypeHandlerYaml() {
        super(ConfigType.YAML);
    }
    private Yaml newYaml() {

        DumperOptions yamlOptions = new DumperOptions();
        org.yaml.snakeyaml.LoaderOptions loaderOptions = new  org.yaml.snakeyaml.LoaderOptions();
        YamlRepresenter representer = new YamlRepresenter(yamlOptions);

        loaderOptions.setAllowDuplicateKeys(false);
        yamlOptions.setIndent(2);
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        representer.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        return new Yaml(
                new SafeConstructor(loaderOptions),
                representer,
                yamlOptions,
                loaderOptions
        );
    }

    public class YamlRepresenter extends Representer {
        public YamlRepresenter(DumperOptions dumperOptions) {
            super(dumperOptions);
            this.multiRepresenters.put(Config.class, new RepresentConfig());
        }

        private class RepresentConfig implements Represent {
            @Override
            public Node representData(Object data) {
                return representers.get(Object.class).representData(((Config) data).toMap());
            }
        }
    }
    @Override
    protected Map<String, Object> parseToMap(String input) {
        Map<String,Object> map = newYaml().load(input);
        if(map == null){
            return new HashMap<>();
        }
        return map;
    }

    @Override
    public String toString(Map<String, Object> map) {
        return newYaml().dump(map);
    }

}
